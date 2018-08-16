package com.twitterscraper.utils;

import java.text.DecimalFormat;

public class GeneralUtils {

    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##.##%");

    public static void safeSleep(final long ms) {
        final long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < ms) ;
    }

    public static String toPercentString(final double d) {
        return DECIMAL_FORMAT.format(d);
    }

    public static void async(final Runnable runnable) {
        new Thread(runnable).start();
    }
}
