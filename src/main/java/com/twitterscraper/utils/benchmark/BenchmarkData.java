package com.twitterscraper.utils.benchmark;

/**
 * Class used by the BenchmarkTimer to track timers
 */
public class BenchmarkData {

    private boolean logAbsolute;
    private final long startTime;
    private final long limit;
    private final String name;

    private BenchmarkData(final String name, final long limit) {
        this.name = name;
        this.startTime = System.currentTimeMillis();
        this.limit = limit;
        this.logAbsolute = false;
    }

    /**
     * Generate an instance of this object
     *
     * @param name  - Name of the Benchmark
     * @param limit - Limit at which to log this benchmark's data
     * @return New instance of BenchmarkData
     */
    public static BenchmarkData data(final String name, final long limit) {
        return new BenchmarkData(name, limit);
    }

    /**
     * @return limit
     */
    public long getLimit() {
        return limit;
    }

    /**
     * @return start time
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    public BenchmarkData logAbsolute(final boolean logAbsolute) {
        this.logAbsolute = logAbsolute;
        return this;
    }

    public boolean getLogAbsolute() {
        return logAbsolute;
    }
}
