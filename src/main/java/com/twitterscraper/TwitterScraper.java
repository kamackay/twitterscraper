package com.twitterscraper;

import com.google.gson.Gson;
import com.twitterscraper.logging.Logger;
import com.twitterscraper.model.Config;
import com.twitterscraper.twitter.utils.TweetPrinter;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;


class TwitterScraper {

    private Map<String, RateLimitStatus> limitMap;

    private static final Logger logger = new Logger(TwitterScraper.class);
    private List<Query> queries;
    private final Twitter twitter;
    private Consumer<Status> handleTweet;
    private List<Status> tweets;

    private static final String RATE_LIMIT_STATUS = "/application/rate_limit_status";
    private static final String SEARCH_TWEETS = "/search/tweets";

    TwitterScraper() {
        twitter = getTwitter();
        tweets = new ArrayList<>();
        queries = new ArrayList<>();
        resetLimitMap();
        setQueries();
    }

    private Twitter getTwitter() {
        return new TwitterFactory(new ConfigurationBuilder()
                .setTweetModeExtended(true)
                .build())
                .getInstance();
    }

    /**
     * Run the configured Queries and handle the results
     */
    void run(final boolean resetList) {
        if (resetList) setQueries();
        new Thread(() -> {
            //checkLimits();
            queries.forEach(query -> {
                try {
                    QueryResult result;
                    result = twitter.search(query);
                    logger.log("");
                    logger.log("Results for: " + query.toString());
                    logger.log("");
                    result.getTweets().forEach(this::handleTweet);
                } catch (Exception e) {
                    logger.e(e);
                }
            });
            try {
                logger.json(tweets, "tweets.json");
            } catch (IOException e) {
                logger.e(e);
            }
            try {
                resetLimitMap();
                boolean ready = waitOnLimit(RATE_LIMIT_STATUS, 1);
                ready &= waitOnLimit(SEARCH_TWEETS, 2);
                if (ready) run(true);
            } catch (InterruptedException e) {
                logger.e(e);
            }
        }).run();
    }

    private boolean waitOnLimit(final String limitName, final int minLimit) throws InterruptedException {
        final RateLimitStatus limit = limitMap.get(limitName);
        if (limit == null) {
            Thread.sleep(1000);
            return false;
        }
        logger.log(String.format("Limit for %s is %d", limitName, limit.getRemaining()));
        if (limit.getRemaining() <= minLimit) {
            final long sleep = limit.getSecondsUntilReset();
            logger.log(String.format("Sleeping for %d seconds to refresh %s limit",
                    sleep,
                    limitName));
            Thread.sleep(sleep * 1000);
        } else {
            logger.log(String.format("%d requests remaining for %s",
                    limit.getRemaining(),
                    limitName));
        }
        return true;
    }

    public void run() {
        run(false);
    }

    private void setQueries() {
        Optional.ofNullable(getConfig()).ifPresent(config -> setQueryList(config.convertQueries()));
    }

    TwitterScraper setTweetHandler(Consumer<Status> handleTweet) {
        this.handleTweet = handleTweet;
        return this;
    }

    TwitterScraper setQueryList(final List<Query> queries) {
        this.queries.clear();
        this.queries.addAll(queries);
        return this;
    }

    private void handleTweet(Status tweet) {
        tweets.add(tweet);
        Optional.ofNullable(handleTweet).orElse(TwitterScraper::printTweet).accept(tweet);
    }

    static void printTweet(Status tweet) {
        logger.log(new TweetPrinter(tweet).toString());
    }

    private void resetLimitMap() {
        try {
            limitMap = twitter.getRateLimitStatus();
        } catch (TwitterException e) {
            limitMap = new HashMap<>();
        }
    }

    private void checkLimits() {
        try {
            logger.log("Checking tweets as " + twitter.getScreenName());
            twitter.getRateLimitStatus().forEach((key, value) -> {
                if (value.getRemaining() != value.getLimit()) {
                    logger.log(String.format("%s: %d/%d - %d seconds",
                            key,
                            value.getRemaining(),
                            value.getLimit(),
                            value.getSecondsUntilReset()));
                }
            });
        } catch (TwitterException e) {
            logger.e(e);
        }
        logger.log("");
    }


    /**
     * Get the config from the config.json file
     *
     * @return The Config Object, converted from json
     */
    private static Config getConfig() {
        try {
            return new Gson().fromJson(new FileReader("config.json"), Config.class);
        } catch (FileNotFoundException e) {
            logger.e(e);
            return null;
        }
    }
}
