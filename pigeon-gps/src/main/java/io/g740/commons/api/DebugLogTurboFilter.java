package io.g740.commons.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import com.google.common.base.Strings;
import org.slf4j.MDC;
import org.slf4j.Marker;

/**
 * @author bbottong
 */
public class DebugLogTurboFilter extends TurboFilter {
    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String s, Object[] objects, Throwable throwable) {
        int levelInt = level.toInt();
        String logLevelValue = MDC.get("logLevel");
        if (Strings.isNullOrEmpty(logLevelValue) ||
                "DEBUG,INFO,TRACE,WARN".indexOf(logLevelValue) == -1) {
            return FilterReply.NEUTRAL;
        }

        if ((Level.TRACE.levelStr.equals(logLevelValue) && levelInt >= Level.TRACE_INT) ||
                (Level.DEBUG.levelStr.equals(logLevelValue) && levelInt >= Level.DEBUG_INT) ||
                (Level.INFO.levelStr.equals(logLevelValue) && levelInt >= Level.INFO_INT) ||
                (Level.WARN.levelStr.equals(logLevelValue) && levelInt >= Level.WARN_INT)) {
            return FilterReply.ACCEPT;
        }

        return FilterReply.DENY;
    }
}
