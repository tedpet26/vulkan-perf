package dev.vulkanperf.client.imfast;

import net.minecraft.client.gui.navigation.ScreenRectangle;

import java.util.IdentityHashMap;
import java.util.List;

/**
 * Bounds-union index for {@code GuiRenderState#hasIntersection} lists. Vanilla scans every
 * element of a node's list (iterator + virtual bounds() + 8 comparisons) for each added
 * glyph/text/item; a HUD-heavy frame repeats that hundreds of times. Keeping a merged
 * rectangle per list lets the common no-overlap case reject with one rectangle test.
 *
 * The union is a superset of the bounds actually present in the list: it only ever grows,
 * so a stale entry can at worst fall back to the vanilla scan, never skip it.
 */
public final class GuiIntersectionIndex {
	private static final IdentityHashMap<List<?>, int[]> UNIONS = new IdentityHashMap<>(64);

	private GuiIntersectionIndex() {
	}

	/** Merge {@code bounds} into the union kept for {@code list}. Call after the element joins the list. */
	public static void track(List<?> list, ScreenRectangle bounds) {
		if (bounds == null) {
			return;
		}
		int[] u = UNIONS.get(list);
		if (u == null) {
			UNIONS.put(list, new int[] {bounds.left(), bounds.top(), bounds.right(), bounds.bottom()});
		} else {
			if (bounds.left() < u[0]) u[0] = bounds.left();
			if (bounds.top() < u[1]) u[1] = bounds.top();
			if (bounds.right() > u[2]) u[2] = bounds.right();
			if (bounds.bottom() > u[3]) u[3] = bounds.bottom();
		}
	}

	/**
	 * True when {@code bounds} cannot intersect anything in {@code list} (no union yet means
	 * "unknown, run the vanilla scan"). Mirrors vanilla's strict-intersection semantics.
	 */
	public static boolean cannotIntersect(List<?> list, ScreenRectangle bounds) {
		int[] u = UNIONS.get(list);
		if (u == null) {
			return false;
		}
		return u[0] >= bounds.right() || u[2] <= bounds.left() || u[1] >= bounds.bottom() || u[3] <= bounds.top();
	}

	/** Drop all unions; vanilla rebuilds the node tree every frame via {@code GuiRenderState#reset}. */
	public static void clear() {
		UNIONS.clear();
	}
}
