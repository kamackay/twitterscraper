package com.twitterscraper.twitter;

import com.google.common.collect.Maps;
import com.twitterscraper.utils.Elective;
import com.twitterscraper.utils.Task;
import com.twitterscraper.utils.benchmark.Benchmark;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Map;
import java.util.Timer;
import java.util.concurrent.Semaphore;
import java.util.function.Predicate;

import static com.twitterscraper.db.Transforms.millisToReadableTime;
import static com.twitterscraper.twitter.RateLimit.RATE_LIMIT_STATUS;
import static com.twitterscraper.twitter.RateLimit.SEARCH_TWEETS;
import static com.twitterscraper.twitter.RateLimit.STATUSES_SHOW;
import static com.twitterscraper.utils.Utils.getLogger;
import static com.twitterscraper.utils.Utils.padString;

@Slf4j
public class TwitterWrapper {

  private static TwitterWrapper instance = null;
  private final Twitter twitter;
  private final Map<String, RateLimitStatus> limitMap;
  private final Semaphore sem = new Semaphore(1, true);
  private int queryCount = 4;

  private TwitterWrapper() {
    twitter = getTwitter();
    limitMap = Maps.newHashMap();
    init();
  }

  public static TwitterWrapper twitter() {
    if(instance == null) {
      instance = new TwitterWrapper();
    }
    return instance;
  }

  public static TwitterWrapper twitter(final int queryCount) {
    if(instance == null) {
      instance = new TwitterWrapper();
    }
    return instance.setQueryCount(queryCount);
  }

  /**
   * Get the amount of time to wait between query loops
   *
   * @param queryCount - Number of queries
   * @return ms to wait
   */
  public static long getWaitTimeForQueries(final int queryCount) {
    return Math.max((int) Math.pow(queryCount, 2), 60) * 500;
  }

  protected TwitterWrapper setQueryCount(final int queryCount) {
    this.queryCount = queryCount;
    return this;
  }

  private void init() {
    resetLimitMap(false);
    new Timer().schedule(new Task(this::resetLimitMap), 0, getWaitTimeForQueries(queryCount) * 10);
  }

  /**
   * Search for a Tweet with the given query
   *
   * @param query - Query to use to search
   * @return QueryResult Object
   * @throws TwitterException
   * @throws InterruptedException
   */
  QueryResult search(Query query) throws TwitterException, InterruptedException {
    try {
      sem.acquire();
      return twitter.search(query);
    } finally {
      sem.release();
    }
  }

  /**
   * Search for a given query without risk of an exception
   *
   * @param query - Query to use to search
   * @return - Elective Result of the search
   */
  public Elective<QueryResult> searchSafe(Query query) {
    try {
      waitOnLimitSafe(SEARCH_TWEETS, 1);
      return Elective.of(search(query));
    } catch (TwitterException e) {
      log.error("Error searching Twitter with Query: {}", query.toString(), e);
      return Elective.empty();
    } catch (InterruptedException e) {
      log.error("Error acquiring semaphore", e);
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
      try {
        if(wait) waitOnLimitSafe(RATE_LIMIT_STATUS, 5);
        sem.acquire();
        limitMap.putAll(twitter.getRateLimitStatus());
      } catch (TwitterException e) {
        log.error("Error Resetting Limit Map", e);
        Thread.sleep(60000); // Don't try again for 1 minute
      }
    } catch (InterruptedException e) {
      log.error("Error Acquiring semaphore", e);
    } finally {
      sem.release();
    }
  }

  /**
   * Wait on a Twitter Limit
   *
   * @param rateLimit - Limit to check
   * @param minLimit  - Min Number to keep the unit at
   * @return True if waiting was successful
   * @throws InterruptedException
   */
  public boolean waitOnLimit(final RateLimit rateLimit, final int minLimit) throws InterruptedException {
    return waitOnLimit(rateLimit, minLimit, false);
  }

  /**
   * Wait on a Twitter Limit
   *
   * @param rateLimit - Limit to check
   * @param minLimit  - Min Number to keep the unit at
   * @param log       - Whether to log the wait
   * @return True if waiting was successful
   * @throws InterruptedException
   */
  public synchronized boolean waitOnLimit(final RateLimit rateLimit, final int minLimit, final boolean log)
      throws InterruptedException {
    try {
      sem.acquire();
      if(limitMap.isEmpty()) return false;
      Elective<RateLimitStatus> limit = getLimit(rateLimit.getName());
      if(!limit.isPresent()) {
        Thread.sleep(1000);
        return false;
      }
      RateLimitStatus status = limit.get();
      if(log) this.log.info("Limit {} is at {}", rateLimit.getName(), status.getRemaining());
      if(status.getRemaining() <= minLimit) {
        final long sleep = status.getSecondsUntilReset() + 1;
        // Extra second to account for race conditions
        this.log.info("Sleeping for {} to refresh \"{}\" limit",
            millisToReadableTime(sleep * 1000),
            rateLimit.getName());
        if(sleep >= 0) Thread.sleep(sleep * 1000);
      }
      return true;
    } finally {
      sem.release();
    }
  }

  /**
   * Wait on a Twitter Limit without exceptions
   *
   * @param rateLimit - Limit to check
   * @param minLimit  - Min Number to keep the unit at
   * @return True if waiting was successful
   */
  public boolean waitOnLimitSafe(final RateLimit rateLimit, final int minLimit) {
    try {
      return waitOnLimit(rateLimit, minLimit);
    } catch (InterruptedException e) {
      log.error("Error waiting on Limit", e);
      return false;
    }
  }

  public Elective<RateLimitStatus> getLimit(final String name) {
    return Elective.ofNullable(limitMap.get(name));
  }

  /**
   * Get a tweet by its ID
   *
   * @param id - ID of tweet to get
   * @return Requested Tweet
   * @throws TwitterException
   * @throws InterruptedException
   */
  @Benchmark
  public Status getTweet(final long id) throws TwitterException, InterruptedException {
    try {
      waitOnLimitSafe(STATUSES_SHOW, 5);
      sem.acquire();
      return twitter.showStatus(id);
    } finally {
      sem.release();
    }
  }

  /**
   * Get a tweet by its ID, safe from exceptions
   *
   * @param id - ID of tweet to get
   * @return Elective of the Status
   */
  public Elective<Status> getTweetSafe(final long id) {
    try {
      return Elective.of(getTweet(id));
    } catch (TwitterException | InterruptedException | NullPointerException e) {
      //logger.error("Error getting tweet, Code 179 means that the tweet is private", e);
      return Elective.empty();
    }

  }

  public void logAllLimits() {
    final Predicate<Map.Entry<String, RateLimitStatus>> filter = entry ->
        entry.getValue().getLimit() != entry.getValue().getRemaining();
    //final Predicate<Map.Entry<String, RateLimitStatus>> filter = entry -> true;

    final int longestName = limitMap.entrySet()
        .stream()
        .filter(filter)
        .map(Map.Entry::getKey)
        .mapToInt(String::length)
        .max()
        .orElse(20);

    limitMap.entrySet().stream()
        .filter(filter)
        .forEach(set -> {
          final RateLimitStatus limit = set.getValue();
          log.info("\tLimit: {}: {} - Resets in {} seconds",
              padString(set.getKey(), longestName + 2),
              padString(String.valueOf(limit.getRemaining()), 6),
              padString(String.valueOf(limit.getSecondsUntilReset()), 4, true));
        });
  }
}
