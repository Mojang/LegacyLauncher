package net.minecraft.launchwrapper;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A wrapper for the log4j logger.
 * 
 * @author Erik Broes and cpw
 *
 */
public class LogWrapper {
	/**
	 * The instance.
	 */
    public static LogWrapper log = new LogWrapper();
    private Logger myLog;

    private static boolean configured;

    private static void configureLogging() {
        log.myLog = LogManager.getLogger("LaunchWrapper");
        configured = true;
    }
    
    /**
     * Redirects this logger.
     * 
     * @param to The logger to redirect to.
     */
    public static void retarget(Logger to) {
        log.myLog = to;
    }
    
    /**
     * Logs a message.
     * 
     * @param logChannel The channel to log on.
     * @param level The severity level to log at.
     * @param format The text.
     * @param data The data to put in the placeholders.
     */
    public static void log(String logChannel, Level level, String format, Object... data) {
        makeLog(logChannel);
        LogManager.getLogger(logChannel).log(level, String.format(format, data));
    }
    
    /**
     * Logs a message.
     * 
     * @param level The severity level to log at.
     * @param format The text.
     * @param data The data to put in the placeholders.
     */
    public static void log(Level level, String format, Object... data) {
        if (!configured) {
            configureLogging();
        }
        log.myLog.log(level, String.format(format, data));
    }
    
    /**
     * Logs a message.
     * 
     * @param logChannel The channel to log on.
     * @param level The severity level to log at.
     * @param ex The exception that occurred.
     * @param format The text.
     * @param data The data to put in the placeholders.
     */
    public static void log(String logChannel, Level level, Throwable ex, String format, Object... data) {
        makeLog(logChannel);
        LogManager.getLogger(logChannel).log(level, String.format(format, data), ex);
    }
    
    /**
     * Logs a message.
     * 
     * @param level The severity level to log at.
     * @param ex The exception that occurred.
     * @param format The text.
     * @param data The data to put in the placeholders.
     */
    public static void log(Level level, Throwable ex, String format, Object... data) {
        if (!configured) {
            configureLogging();
        }
        log.myLog.log(level, String.format(format, data), ex);
    }
    
    /**
     * Logs a message at level SEVERE.
     * 
     * @param format The text.
     * @param data The data to put in the placeholders.
     */
    public static void severe(String format, Object... data) {
        log(Level.ERROR, format, data);
    }
    
    /**
     * Logs a message at level WARNING.
     * 
     * @param format The text.
     * @param data The data to put in the placeholders.
     */
    public static void warning(String format, Object... data) {
        log(Level.WARN, format, data);
    }
    
    /**
     * Logs a message at level INFO.
     * 
     * @param format The text.
     * @param data The data to put in the placeholders.
     */
    public static void info(String format, Object... data) {
        log(Level.INFO, format, data);
    }
    
    /**
     * Logs a message at level FINE.
     * 
     * @param format The text.
     * @param data The data to put in the placeholders.
     */
    public static void fine(String format, Object... data) {
        log(Level.DEBUG, format, data);
    }
    
    /**
     * Logs a message at level FINER.
     * 
     * @param format The text.
     * @param data The data to put in the placeholders.
     */
    public static void finer(String format, Object... data) {
        log(Level.TRACE, format, data);
    }
    
    /**
     * Logs a message at level FINEST.
     * 
     * @param format The text.
     * @param data The data to put in the placeholders.
     */
    public static void finest(String format, Object... data) {
        log(Level.TRACE, format, data);
    }
    
    /**
     * Initializes a channel.
     * 
     * @param logChannel The channel to initialize.
     */
    public static void makeLog(String logChannel) {
        LogManager.getLogger(logChannel);
    }
}
