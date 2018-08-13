package com.twitterscraper;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.model.Config;
import com.twitterscraper.model.Query;
import com.twitterscraper.monitors.AbstractMonitor;
import com.twitterscraper.utils.Elective;
import com.twitterscraper.utils.benchmark.Benchmark;
import org.slf4j.LoggerFactory;
import twitter4j.QueryResult;
import twitter4j.Status;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.twitterscraper.db.Transforms.millisToReadableTime;
import static com.twitterscraper.twitter.TwitterWrapper.getWaitTimeForQueries;
import static com.twitterscraper.twitter.TwitterWrapper.twitter;
import static com.twitterscraper.utils.benchmark.BenchmarkData.data;
import static com.twitterscraper.utils.benchmark.BenchmarkTimer.timer;


public class TwitterScraper {

    private org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());
    private final List<com.twitterscraper.model.Query> queries;

    private final DatabaseWrapper db;
    private final Set<AbstractMonitor> monitors;

    @Inject
    TwitterScraper(final DatabaseWrapper db) {
        this.db = db;
        queries = new ArrayList<>();
        monitors = new HashSet<>();
        //monitors.addAll(Sets.newHashSet(new UpdateMonitor(db)));
        setQueries();
    }

    /**
     * Run the configured Queries and handle the results
     */
    void start() {
        new Thread(this::run)
                .start();
        monitors.forEach(AbstractMonitor::start);
    }

    // TODO set this up in it's own Monitor
    private void run() {
        try {
            while (true) {
                timer().setLogLimit(100)
                        .start(data("SetQueries", 10));
                setQueries();
                timer().end("SetQueries")
                        .start("ResetLimitMap");
                timer().end("ResetLimitMap");
                try {
                    final long ms = getWaitTimeForQueries(queries.size());
                    logger.info("Waiting for {} to span out API requests", millisToReadableTime(ms));
                    //Thread.sleep(ms);
                } catch (Exception e) {
                    logger.error("Error spacing out API Requests", e);
                    continue;
                }
                queries.parallelStream().forEach(query -> handleQuery(query.getName(), query));
            }
        } catch (Exception e) {
            logger.error("Exception running TwitterScraper", e);
        }
    }

    @Benchmark(paramName = true)
    void handleQuery(final String queryName, final Query query) {
        try {
            db.verifyIndex(queryName);
            if (!query.getModel().getUpdateExisting())
                query.getQuery().sinceId(db.getMostRecent(queryName));

            final Elective<QueryResult> safeResult = twitter().searchSafe(query.getQuery());
            if (!safeResult.isPresent()) {
                logger.error("Error fetching results for Query " + queryName);
                return;
            }
            safeResult.ifPresent(result -> this.handleResult(result, queryName));
        } catch (Exception e) {
            logger.error("Error handling query " + queryName, e);
        }
    }

    private void handleResult(final QueryResult result, final String queryName) {
        final List<Status> tweets = result.getTweets();
        final long newTweets = tweets.parallelStream()
                .filter(tweet -> db.upsert(tweet, queryName))
                .count();
        if (newTweets > 0)
            logger.info("Query {} returned {} results, {} of which were new",
                    queryName,
                    tweets.size(),
                    newTweets);
    }

    void setQueries() {
        Elective.ofNullable(getConfig())
                .ifPresent(config -> setQueryList(config.convertQueries()))
                .orElse(() -> logger.error("Could not load config"));
    }

    TwitterScraper setQueryList(final List<com.twitterscraper.model.Query> queries) {
        this.queries.clear();
        this.queries.addAll(queries);
        monitors.forEach(abstractMonitor ->
                abstractMonitor.setQueries(new ArrayList<>(this.queries)));
        return this;
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
            logger.error("Error Finding Config File", e);
            return null;
        }
    }
}
