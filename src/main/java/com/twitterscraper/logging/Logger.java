package com.twitterscraper.logging;

import java.io.File;
import java.io.FileWriter;

/**
 * Placeholder logging class
 */
public class Logger {

    private final Class classname;
    private static final String filename = "output.log";

    public Logger(Class classname) {
        this.classname = classname;
        File f = new File(filename);
        if (f.exists()) {
            try {
                f.delete();
            } catch (Exception e) {
                // ?
            }
        }
    }

    /**
     * Default logging function
     *
     * @param s - String to log
     */
    public void log(String s) {
        // TODO make this an actual logger
        String text = String.format("%s - %s",
                classname.getName(),
                s);
        System.out.println(text);
        try (FileWriter fw = new FileWriter(filename, true)) {
            fw.append(text + "\n");
        } catch (Exception e) {
            // ?
        }
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
