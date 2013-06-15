package net.minecraft.launchwrapper;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LogWrapper {
    public static LogWrapper log = new LogWrapper();
    private Logger myLog;

    private static boolean configured;

    private static void configureLogging() {
        log.myLog = Logger.getLogger("LaunchWrapper");
        configured = true;
    }

    public static void retarget(Logger to) {
        log.myLog = to;
    }
    public static void log(String logChannel, Level level, String format, Object... data) {
        makeLog(logChannel);
        Logger.getLogger(logChannel).log(level, String.format(format, data));
    }

    public static void log(Level level, String format, Object... data) {
        if (!configured) {
            configureLogging();
        }
        log.myLog.log(level, String.format(format, data));
    }

    public static void log(String logChannel, Level level, Throwable ex, String format, Object... data) {
        makeLog(logChannel);
        Logger.getLogger(logChannel).log(level, String.format(format, data), ex);
    }

    public static void log(Level level, Throwable ex, String format, Object... data) {
        if (!configured) {
            configureLogging();
        }
        log.myLog.log(level, String.format(format, data), ex);
    }

    public static void severe(String format, Object... data) {
        log(Level.SEVERE, format, data);
    }

    public static void warning(String format, Object... data) {
        log(Level.WARNING, format, data);
    }

    public static void info(String format, Object... data) {
        log(Level.INFO, format, data);
    }

    public static void fine(String format, Object... data) {
        log(Level.FINE, format, data);
    }

    public static void finer(String format, Object... data) {
        log(Level.FINER, format, data);
    }

    public static void finest(String format, Object... data) {
        log(Level.FINEST, format, data);
    }

    public static void makeLog(String logChannel) {
        Logger.getLogger(logChannel).setParent(log.myLog);
    }
}
