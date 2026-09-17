package dev.vulkanperf.mixin.mfix;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import dev.vulkanperf.config.PerfConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;

/**
 * Zip packs re-enumerate every central-directory entry for each {@code getNamespaces} and
 * {@code listResources} call; a resource reload performs that walk once per type, namespace and
 * directory. The entry name list is immutable while the pack is open, so it is captured once
 * and scanned as plain strings; single entries are still resolved live through the zip file.
 *
 * <p>The zip handle lives in a private nested holder class that cannot be shadowed directly,
 * so it is reached through a cached {@link MethodHandle} instead (one reflection lookup per
 * mixin class, invoked only during resource reloads).
 */
@Mixin(FilePackResources.class)
public abstract class FilePackResourcesIndexMixin {
	@Shadow
	@Final
	private String prefix;

	@Unique
	private static final org.slf4j.Logger VP_LOGGER = org.slf4j.LoggerFactory.getLogger("vulkanperf");

	@Unique
	private static volatile MethodHandle VP_ZIP_HANDLE;

	@Unique
	private static volatile NoSuchFieldException VP_LOOKUP_FAILURE;

	@Unique
	private boolean vp$indexBuilt;

	@Unique
	private volatile List<String> vp$entryNames;

	@Unique
	private ZipFile vp$zipFile() {
		try {
			MethodHandle handle = VP_ZIP_HANDLE;
			if (handle == null) {
				if (VP_LOOKUP_FAILURE != null) {
					VP_LOGGER.warn("vulkanperf: zip pack index unavailable; resource pack contents may be missing. Cause: {}", VP_LOOKUP_FAILURE.getMessage());
					return null;
				}
				// Both the holder field and its accessor are private in vanilla, so plain
				// getMethod (publics only) silently fails here and would disable every zip
				// resource pack. Use declared-member lookup with access overriding.
				Field field = FilePackResources.class.getDeclaredField("zipFileAccess");
				field.setAccessible(true);
				Object holder = field.get(this);
				java.lang.reflect.Method accessor = holder.getClass().getDeclaredMethod("getOrCreateZipFile");
				accessor.setAccessible(true);
				handle = MethodHandles.lookup().unreflect(accessor).bindTo(holder);
				VP_ZIP_HANDLE = handle;
			}
			return (ZipFile) handle.invoke();
		} catch (Throwable t) {
			if (t instanceof NoSuchFieldException || t instanceof NoSuchMethodException) {
				VP_LOOKUP_FAILURE = new NoSuchFieldException(t.getMessage());
				VP_LOGGER.warn("vulkanperf: zip pack index unavailable; resource pack contents may be missing. Cause: {}", t.toString());
			} else {
				VP_LOGGER.warn("vulkanperf: failed to open zip pack handle", t);
			}
			return null;
		}
	}

	@Unique
	private synchronized List<String> vp$buildIndex() {
		// Resource reloads query packs from worker threads; the one-time enumeration must
		// not race another thread's build.
		List<String> names = this.vp$entryNames;
		if (names == null) {
			names = new ArrayList<>(1024);
			ZipFile zipFile = this.vp$zipFile();
			if (zipFile != null) {
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					if (!entry.isDirectory()) {
						names.add(entry.getName());
					}
				}
			}
			this.vp$entryNames = names;
		}
		this.vp$indexBuilt = true;
		return names;
	}

	/**
	 * @reason scan a cached name list instead of the zip central directory
	 * @author vulkan-perf
	 */
	@Overwrite
	public Set<String> getNamespaces(final PackType type) {
		if (!PerfConfig.get().mfix.zipIndex) {
			return vp$vanillaGetNamespaces(type);
		}
		List<String> names = this.vp$indexBuilt ? this.vp$entryNames : this.vp$buildIndex();
		String typePrefix = this.addPrefix(type.getDirectory() + "/");
		Set<String> namespaces = new HashSet<>();
		for (int i = 0; i < names.size(); i++) {
			String namespace = FilePackResources.extractNamespace(typePrefix, names.get(i));
			if (!namespace.isEmpty() && Identifier.isValidNamespace(namespace)) {
				namespaces.add(namespace);
			}
		}
		return namespaces;
	}

	@Unique
	private Set<String> vp$vanillaGetNamespaces(final PackType type) {
		ZipFile zipFile = this.vp$zipFile();
		if (zipFile == null) {
			return Set.of();
		}
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		Set<String> namespaces = new HashSet<>();
		String typePrefix = this.addPrefix(type.getDirectory() + "/");
		while (entries.hasMoreElements()) {
			String namespace = FilePackResources.extractNamespace(typePrefix, entries.nextElement().getName());
			if (!namespace.isEmpty() && Identifier.isValidNamespace(namespace)) {
				namespaces.add(namespace);
			}
		}
		return namespaces;
	}

	/**
	 * @reason hash-lookup matching entries instead of enumerating the whole archive
	 * @author vulkan-perf
	 */
	@Overwrite
	public void listResources(final PackType type, final String namespace, final String directory, final PackResources.ResourceOutput output) {
		ZipFile zipFile = this.vp$zipFile();
		if (zipFile == null) {
			return;
		}
		List<String> names = this.vp$indexBuilt ? this.vp$entryNames : this.vp$buildIndex();
		String root = this.addPrefix(type.getDirectory() + "/" + namespace + "/");
		String prefix = root + directory + "/";
		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			if (name.startsWith(prefix)) {
				String path = name.substring(root.length());
				Identifier id = Identifier.tryBuild(namespace, path);
				if (id != null) {
					ZipEntry entry = zipFile.getEntry(name);
					if (entry != null) {
						output.accept(id, IoSupplier.create(zipFile, entry));
					}
				} else {
					VP_LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", namespace, path);
				}
			}
		}
	}

	@Unique
	private String addPrefix(final String path) {
		return this.prefix.isEmpty() ? path : this.prefix + "/" + path;
	}
}
