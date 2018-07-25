package com.twitterscraper.logging;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Placeholder logging class
 */
@Deprecated // Using SLF4J now
public class Logger {

    private final Class classname;
    private static final String filename = "output.log";
    private final Gson gson;
    private static final DateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public Logger(final Class classname) {
        this.classname = classname;
        this.gson = new Gson();
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
    public void log(final String s) {
        // TODO make this an actual logger
        String text = String.format("%s - %s: %s",
                getCurrentTimestamp(),
                classname.getName(),
                s);
        System.out.println(text);
        try (FileWriter fw = new FileWriter(filename, true)) {
            fw.append(text + "\n");
        } catch (Exception e) {
            // ?
        }
    }

    private String getCurrentTimestamp() {
        return DATE_FORMAT.format(new Date());
    }

    public void e(final String message, final Throwable e) {
        log(String.format("%s - %s", message, e.getMessage()));
        logStackTrace(e);
    }

    private void logStackTrace(final Throwable e) {
        for (StackTraceElement element : e.getStackTrace()) {
            log("\t" + element.toString());
        }
    }

    /**
     * Log the given exception
     *
     * @param e - Exception to log
     */
    public void e(final Throwable e) {
        log(e.getMessage());
        logStackTrace(e);
    }

    /**
     * Write the given object to a file - Json encoded
     *
     * @param o        - Object to json encode
     * @param filename - name of the file to write the object to
     * @throws IOException If there is an issue writing to the file
     */
    public void json(Object o, String filename) throws IOException {
        try (Writer writer = new FileWriter(filename)) {
            gson.toJson(o, writer);
        }
    }
}