package dev.vulkanperf.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.filter.AbstractFilter;

import java.util.Locale;

public final class LogSpamFilter extends AbstractFilter {
	private LogSpamFilter() {
		super(Filter.Result.DENY, Filter.Result.NEUTRAL);
	}

	public static void install() {
		if (LogManager.getContext(false) instanceof LoggerContext context) {
			Configuration configuration = context.getConfiguration();
			LogSpamFilter filter = new LogSpamFilter();
			configuration.getRootLogger().addFilter(filter);
			context.updateLoggers();
		}
	}

	@Override
	public Result filter(LogEvent event) {
		if (event.getLevel().intLevel() < Level.WARN.intLevel()) {
		 return Result.NEUTRAL;
		}
		String message = event.getMessage().getFormattedMessage();
		if (message == null) {
			return Result.NEUTRAL;
		}
		String lower = message.toLowerCase(Locale.ROOT);
		if (lower.contains("unknown pcie device")
			|| lower.contains("error reading existing property")
			|| lower.contains("non [a-z0-9/._-] character in path")
			|| lower.contains("ambiguous crafting recipe")
			|| lower.contains("tried to load unrecognized recipe")
			|| lower.contains("missing required property")) {
			return Result.DENY;
		}
		return Result.NEUTRAL;
	}
}
