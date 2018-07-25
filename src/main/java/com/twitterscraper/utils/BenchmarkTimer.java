package com.twitterscraper.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.twitterscraper.db.Transforms.millisToReadableTime;

/**
 * Debugging class to quickly log time that code execution takes
 */
public class BenchmarkTimer {

    private static BenchmarkTimer instance = null;
    private long limit = 0;

    private Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, BenchmarkData> startTimes;

    public static BenchmarkTimer timer() {
        if (instance == null)
            instance = new BenchmarkTimer();

        return instance;
    }

    private BenchmarkTimer() {
        startTimes = new HashMap<>();
    }

    public BenchmarkTimer start(final BenchmarkData data) {
        startTimes.put(data.getName(), data);
        return this;
    }

    public BenchmarkTimer end(final String benchmarkName) {
        final Elective<BenchmarkData> data = get(benchmarkName);
        final long time = System.currentTimeMillis() -
                data.map(BenchmarkData::getStartTime).orElse(-1L);
        if (time > data.map(BenchmarkData::getLimit).orElse(limit)) {
            logger.info(String.format("Benchmark \"%s\" completed in %s",
                    benchmarkName,
                    millisToReadableTime(time)));
        }
        return this;
    }

    /**
     * Set the limit for which anything longer should be logged
     *
     * @param limit - If process takes longer than the limit, it will be logged
     */
    public BenchmarkTimer setLogLimit(final long limit) {
        this.limit = limit;
        return this;
    }

    private Elective<BenchmarkData> get(final String name) {
        return Elective.ofNullable(startTimes.get(name));
    }
}
