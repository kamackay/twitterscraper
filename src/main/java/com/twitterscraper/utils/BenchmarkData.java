package com.twitterscraper.utils;

public class BenchmarkData {
    private final long startTime;
    private final long limit;
    private final String name;

    private BenchmarkData(final String name, final long limit) {
        this.name = name;
        this.startTime = System.currentTimeMillis();
        this.limit = limit;
    }

    public static BenchmarkData data(final String name, final long limit) {
        return new BenchmarkData(name, limit);
    }

    public long getLimit() {
        return limit;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getName() {
        return name;
    }
}
