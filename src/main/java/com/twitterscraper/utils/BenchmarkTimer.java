package com.twitterscraper.utils;

import com.twitterscraper.logging.Logger;

import java.util.HashMap;
import java.util.Map;

import static com.twitterscraper.db.Transforms.millisToReadableTime;

/**
 * Debugging class to quickly log time that code execution takes
 */
public class BenchmarkTimer {

    private static BenchmarkTimer instance = null;
    private long limit = 0;

    private final Logger logger = new Logger(getClass());
    private final Map<String, Long> startTimes;

    public static BenchmarkTimer timer() {
        if (instance == null)
            instance = new BenchmarkTimer();

        return instance;
    }

    private BenchmarkTimer() {
        startTimes = new HashMap<>();
    }

    public BenchmarkTimer start(final String benchmarkName) {
        startTimes.put(benchmarkName, System.currentTimeMillis());
        return this;
    }

    public BenchmarkTimer end(final String benchmarkName) {
        final long time = System.currentTimeMillis() - startTimes.getOrDefault(benchmarkName, 0L);
        if (time > limit) {
            logger.log(String.format("Benchmark %s completed in %s",
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

}
