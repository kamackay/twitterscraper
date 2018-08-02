package com.twitterscraper.twitter;

import com.google.common.collect.Maps;
import com.twitterscraper.utils.Elective;
import com.twitterscraper.utils.Task;
import org.slf4j.LoggerFactory;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Map;
import java.util.Timer;
import java.util.concurrent.Semaphore;

import static com.twitterscraper.db.Transforms.millisToReadableTime;
import static com.twitterscraper.twitter.RateLimit.RATE_LIMIT_STATUS;
import static com.twitterscraper.twitter.RateLimit.SEARCH_TWEETS;
import static com.twitterscraper.twitter.RateLimit.STATUSES_SHOW;

public class TwitterWrapper {

    private org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    private static TwitterWrapper instance = null;

    private final Twitter twitter;
    private final Map<String, RateLimitStatus> limitMap;
    private final Semaphore sem = new Semaphore(1, true);

    private TwitterWrapper() {
        twitter = getTwitter();
        limitMap = Maps.newHashMap();
        init();
    }

    private void init() {
        resetLimitMap(false);
        new Timer().schedule(new Task(this::resetLimitMap), 0, 2000);
    }

    public static TwitterWrapper twitter() {
        if (instance == null) {
            instance = new TwitterWrapper();
        }

        return instance;
    }

    QueryResult search(Query query) throws TwitterException, InterruptedException {
        try {
            sem.acquire();
            return twitter.search(query);
        } finally {
            sem.release();
        }
    }

    public Elective<QueryResult> searchSafe(Query query) {
        try {
            waitOnLimitSafe(SEARCH_TWEETS, 1);
            return Elective.of(search(query));
        } catch (TwitterException e) {
            logger.error("Error searching Twitter", e);
            return Elective.empty();
        } catch (InterruptedException e) {
            logger.error("Error acquiring semaphore", e);
            return Elective.empty();
        }
    }

    private Twitter getTwitter() {
        return new TwitterFactory(new ConfigurationBuilder()
                .setGZIPEnabled(true)
                .setTweetModeExtended(true)
                .build())
                .getInstance();
    }

    private void resetLimitMap() {
        resetLimitMap(true);
    }

    private void resetLimitMap(final boolean wait) {
        try {
            if (wait) waitOnLimitSafe(RATE_LIMIT_STATUS, 5);
            sem.acquire();
            limitMap.putAll(twitter.getRateLimitStatus());
        } catch (TwitterException e) {
            logger.error("Error Resetting Limit Map", e);
        } catch (InterruptedException e) {
            logger.error("Error Acquiring semaphore", e);
        } finally {
            sem.release();
        }
    }

    public boolean waitOnLimit(final RateLimit rateLimit, final int minLimit) throws InterruptedException {
        return waitOnLimit(rateLimit, minLimit, false);
    }

    public boolean waitOnLimit(final RateLimit rateLimit, final int minLimit, final boolean log)
            throws InterruptedException {
        try {
            sem.acquire();
            if (limitMap.isEmpty()) return false;
            Elective<RateLimitStatus> limit = getLimit(rateLimit.getName());
            if (!limit.isPresent()) {
                Thread.sleep(1000);
                return false;
            }
            RateLimitStatus status = limit.get();
            if (log) logger.info("Limit {} is at {}", rateLimit.getName(), status.getRemaining());
            if (status.getRemaining() <= minLimit) {
                final long sleep = status.getSecondsUntilReset() + 1;
                // Extra second to account for race conditions
                logger.info("Sleeping for {} to refresh \"{}\" limit",
                        millisToReadableTime(sleep * 1000),
                        rateLimit.getName());
                if (sleep >= 0) Thread.sleep(sleep * 1000);
            }
            return true;
        } finally {
            sem.release();
        }
    }

    public boolean waitOnLimitSafe(final RateLimit rateLimit, final int minLimit) {
        try {
            return waitOnLimit(rateLimit, minLimit);
        } catch (InterruptedException e) {
            logger.error("Error waiting on Limit", e);
            return false;
        }
    }

    private Elective<RateLimitStatus> getLimit(final String name) {
        return Elective.ofNullable(limitMap.get(name));
    }

    public Status getTweet(final long id) throws TwitterException, InterruptedException {
        try {
            waitOnLimitSafe(STATUSES_SHOW, 5);
            sem.acquire();
            return twitter.showStatus(id);
        } catch (TwitterException e) {
            throw e;
        } finally {
            sem.release();
        }
    }

    public Elective<Status> getTweetSafe(final long id) {
        try {
            return Elective.of(getTweet(id));
        } catch (TwitterException | InterruptedException | NullPointerException e) {
            return Elective.empty();
        }

    }

    private void logAllLimits() {
        limitMap.forEach((s, rateLimitStatus) -> {
            if (rateLimitStatus.getLimit() != rateLimitStatus.getRemaining())
                logger.info("\tLimit: {}: {}",
                        s, rateLimitStatus.getRemaining());
        });
    }
}
