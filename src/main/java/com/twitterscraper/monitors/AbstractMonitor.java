package com.twitterscraper.monitors;

import com.twitterscraper.RateLimit;
import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.model.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.twitterscraper.db.Transforms.millisToReadableTime;

public abstract class AbstractMonitor {

    private Map<String, RateLimitStatus> limitMap;
    final Twitter twitter;
    final DatabaseWrapper db;
    final List<Query> queries;

    public AbstractMonitor(
            final Twitter twitter,
            final DatabaseWrapper db) {
        this.twitter = twitter;
        this.db = db;
        queries = new ArrayList<>();
        resetLimitMap();
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean running;

    public void start() {
        running = true;
        while (running) {
            try {
                new Thread(this::run).start();
                Thread.sleep(getFrequency() * 1000);
                resetLimitMap();
            } catch (Exception e) {
                logger.error("Error running Monitor", e);
            }
        }
    }

    public void setQueries(List<Query> queries) {
        this.queries.clear();
        this.queries.addAll(queries);
    }

    public void stop() {
        running = false;
    }

    abstract void run();

    /**
     * @return - Frequency in seconds to run monitor
     */
    abstract int getFrequency();

    private void resetLimitMap() {
        if (limitMap == null) limitMap = new HashMap<>();
        try {
            limitMap.clear();
            limitMap.putAll(twitter.getRateLimitStatus());
        } catch (TwitterException e) {
            limitMap.clear();
        }
    }

    boolean waitOnLimit(final RateLimit rateLimit, final int minLimit) throws InterruptedException {
        final RateLimitStatus limit = limitMap.get(rateLimit.getName());
        if (limit == null) {
            Thread.sleep(1000);
            return false;
        }
        if (limit.getRemaining() <= minLimit) {
            final long sleep = limit.getSecondsUntilReset() + 1;
            // Extra second to account for race conditions
            logger.info("Sleeping for {} to refresh \"{}\" limit",
                    millisToReadableTime(sleep * 1000),
                    rateLimit.getName());
            if (sleep >= 0) Thread.sleep(sleep * 1000);
        }
        return true;
    }

    boolean waitOnLimitSafe(final RateLimit rateLimit, final int minLimit) {
        try {
            return waitOnLimit(rateLimit, minLimit);
        } catch (InterruptedException e) {
            logger.error("Error waiting on Limit", e);
            return false;
        }
    }
}
