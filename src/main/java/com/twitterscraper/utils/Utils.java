package com.twitterscraper.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class Utils {

  public static String formatBytes(final long bytes) {
    double dbytes = (double) bytes;
    short unit = 0;
    while (dbytes > 1024) {
      dbytes /= 1024;
      unit++;
    }
    return String.format("%.4f %s", dbytes, Arrays.asList(
        "bytes",
        "kB",
        "MB",
        "TB", "PB", "EB", "ZB", "YB"
    ).get(unit));
  }

  public static String padString(final String s, final int length) {
    return padString(s, length, false, ' ');
  }

  public static String padString(final String s, final int length, final boolean start) {
    return padString(s, length, start, ' ');
  }

  public static String padString(
      final String s, final int length,
      final boolean start, final char c) {
    final StringBuilder builder = new StringBuilder();
    if (!start) builder.append(s);
    for (int x = 0; x < Math.max(length - s.length(), 0); x++) {
      builder.append(c);
    }
    if (start) builder.append(s);
    return builder.toString();
  }

  public static Logger getLogger(final Class<?> c) {
    return getLogger(c.getSimpleName());
  }

  public static Logger getLogger(final String name) {
    return LoggerFactory.getLogger(name);
  }
}
