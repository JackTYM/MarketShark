package dev.jacktym.marketshark.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class BugLogger {
    private static Logger logger = Logger.getLogger(BugLogger.class.getName());
    private static FileHandler fileHandler;
    private static String logPath = "config/ms-bugfix-log.txt";

    static {
        try {
            // Set up FileHandler to write logs to specified file
            fileHandler = new FileHandler(logPath, true);
            logger.addHandler(fileHandler);

            // Specify the formatter for the logger
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);

            // Set the logger level if you want to filter out less severe messages
            logger.setLevel(Level.ALL);

            // Disabling the console output to the server logs by default
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            BugLogger.logError(e);
        }
    }

    public static void log(String message, boolean printToConsole) {
        // A hack to infer the caller's class name and line number
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        String callerClassName = stackTrace[1].getClassName();
        int callerLineNumber = stackTrace[1].getLineNumber();

        logger.logp(Level.INFO, callerClassName, String.valueOf(callerLineNumber), message);
        if (printToConsole) {
            System.out.println(message);
        }
    }

    public static void logChat(String message, boolean printToChat) {
        // A hack to infer the caller's class name and line number
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        String callerClassName = stackTrace[1].getClassName();
        int callerLineNumber = stackTrace[1].getLineNumber();

        logger.logp(Level.INFO, callerClassName, String.valueOf(callerLineNumber), message);
        if (printToChat) {
            ChatUtils.printMarkedChat(message);
        }
    }

    public static void logError(Throwable e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        String callerClassName = stackTrace[0].getClassName();
        int callerLineNumber = stackTrace[0].getLineNumber();

        logger.logp(Level.SEVERE, callerClassName, String.valueOf(callerLineNumber), e.getMessage(), e);

        e.printStackTrace();

        sendBugLog();
    }

    public static void sendBugLog() {
        try {
            DiscordIntegration.sendNoLog("buglog", Arrays.toString(Files.readAllBytes(Paths.get(logPath))));
        } catch (Exception ignored) {}
    }
}