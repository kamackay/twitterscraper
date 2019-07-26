package com.twitterscraper.utils;

import java.util.Arrays;

public class Utils {

  public static String formatBytes(final long bytes) {
    double dbytes = (double) bytes;
    short unit = 0;
    while (dbytes > 1024) {
      dbytes /= 1024;
      unit++;
    }
    return String.format("%.2f %s", dbytes, Arrays.asList(
        "bytes",
        "kB",
        "MB",
        "TB", "PB", "EB", "ZB", "YB"
    ).get(unit));
  }
}
