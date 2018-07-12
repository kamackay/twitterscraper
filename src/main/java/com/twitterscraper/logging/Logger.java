package com.twitterscraper.logging;

/**
 * Placeholder logging class
 */
public class Logger {

    private final Class classname;

    public Logger(Class classname) {
        this.classname = classname;
    }

    /**
     * Default logging function
     *
     * @param s - String to log
     */
    public void log(String s) {
        // TODO make this an actual logger
        System.out.println(String.format("%s - %s",
                classname.getName(),
                s));
    }

    /**
     * Log the given exception
     *
     * @param e - Exception to log
     */
    public void e(Throwable e) {
        log(e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            log(element.toString());
        }
    }
}
