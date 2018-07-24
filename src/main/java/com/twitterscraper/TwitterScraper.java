package com.twitterscraper;

import com.google.gson.Gson;
import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.logging.Logger;
import com.twitterscraper.model.Config;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

import static com.twitterscraper.db.Transforms.millisToReadableTime;


class TwitterScraper {

    private Map<String, RateLimitStatus> limitMap;

    private final Logger logger = new Logger(getClass());
    private List<com.twitterscraper.model.Query> queries;
    private final Twitter twitter;
    private final DatabaseWrapper db;

    private static final String RATE_LIMIT_STATUS = "/application/rate_limit_status";
    private static final String SEARCH_TWEETS = "/search/tweets";

    TwitterScraper() {
        twitter = getTwitter();
        queries = new ArrayList<>();
        db = new DatabaseWrapper();
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
    void start() {
        new Thread(this::run)
                .start();
    }

    private void run() {
        while (true) {
            setQueries();
            resetLimitMap();
            try {
                boolean ready = waitOnLimit(RATE_LIMIT_STATUS, 2);
                ready &= waitOnLimit(SEARCH_TWEETS, queries.size() + 1);
                if (!ready) {
                    logger.log("Cannot get tweets because of Rate Limits or internet connection");
                    return;
                }
            } catch (InterruptedException e) {
                logger.e("Error waiting on Rate Limits", e);
                return;
            }
            queries.forEach(query -> {
                final String queryName = query.getModel().getQueryName();
                try {
                    db.verifyIndex(queryName);
                    if (!query.getModel().getUpdateExisting()) query.getQuery().sinceId(db.getMostRecent(queryName));
                    // Uncomment this line to make it so that the query only gets new tweets
                    final QueryResult result = twitter.search(query.getQuery());
                    final List<Status> tweets = result.getTweets();
                    final long newTweets = tweets.parallelStream()
                            .filter(tweet -> db.upsert(tweet, queryName))
                            .count();
                    if (newTweets > 0)
                        logger.log(String.format("Query %s returned %d results, %d of which were new",
                                queryName,
                                tweets.size(),
                                newTweets));
                    else logger.log("No new results from " + queryName);
                } catch (Exception e) {
                    logger.e("Error handling query " + queryName, e);
                }
            });
        }
    }

    private boolean waitOnLimit(final String limitName, final int minLimit) throws InterruptedException {
        final RateLimitStatus limit = limitMap.get(limitName);
        if (limit == null) {
            Thread.sleep(1000);
            return false;
        }
        if (limit.getRemaining() <= minLimit) {
            final long sleep = limit.getSecondsUntilReset() + 1;
            // Extra second to account for race conditions
            logger.log(String.format("Sleeping for %s to refresh \"%s\" limit",
                    millisToReadableTime(sleep * 1000),
                    limitName));
            if (sleep >= 0) Thread.sleep(sleep * 1000);
        } //else {
            //logger.log(String.format("%d requests remaining for %s",
            //        limit.getRemaining(),
            //        limitName));
        //}
        return true;
    }

    private void setQueries() {
        Optional.ofNullable(getConfig()).ifPresent(config ->
                setQueryList(config.convertQueries()));
    }

    private TwitterScraper setQueryList(final List<com.twitterscraper.model.Query> queries) {
        this.queries.clear();
        this.queries.addAll(queries);
        return this;
    }

    private void resetLimitMap() {
        if (limitMap == null) limitMap = new HashMap<>();
        try {
            limitMap.clear();
            limitMap.putAll(twitter.getRateLimitStatus());
        } catch (TwitterException e) {
            limitMap.clear();
        }
    }


    /**
     * Get the config from the config.json file
     *
     * @return The Config Object, converted from json
     */
    private Config getConfig() {
        try {
            return new Gson().fromJson(new FileReader("config.json"), Config.class);
        } catch (FileNotFoundException e) {
            logger.e(e);
            return null;
        }
    }
}
