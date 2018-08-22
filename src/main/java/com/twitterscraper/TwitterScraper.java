package com.twitterscraper;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.twitterscraper.analytics.AnalysisService;
import com.twitterscraper.analytics.SentimentAnalyzer;
import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.db.Transforms;
import com.twitterscraper.model.Config;
import com.twitterscraper.model.Query;
import com.twitterscraper.monitors.AbstractService;
import com.twitterscraper.monitors.UpdateService;
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
import static com.twitterscraper.utils.GeneralUtils.async;


public class TwitterScraper {

    private org.slf4j.Logger logger = LoggerFactory.getLogger(TwitterScraper.class);
    private final List<com.twitterscraper.model.Query> queries;

    private final Set<AbstractService> services;
    private final UpdateService updateService;
    private final AnalysisService analysisService;
    private final DatabaseWrapper db;
    private final SentimentAnalyzer analyzer;

    @Inject
    TwitterScraper(
            final UpdateService updateService,
            final AnalysisService analysisService,
            final DatabaseWrapper db,
            final SentimentAnalyzer analyzer) {
        this.updateService = updateService;
        this.analysisService = analysisService;
        this.db = db;
        this.analyzer = analyzer;
        queries = new ArrayList<>();
        services = new HashSet<>();
        reconfigure();
    }

    /**
     * Run the configured Queries and handle the results
     */
    void start() {
        new Thread(this::run)
                .start();
    }

    // TODO set this up in its own Monitor
    private void run() {
        try {
            while (true) {
                reconfigure();
                queries.parallelStream().forEach(query -> handleQuery(query.getName(), query));
                services.forEach(AbstractService::run);

                try {
                    final long ms = getWaitTimeForQueries(queries.size());
                    logger.info("Waiting for {} to span out API requests", millisToReadableTime(ms));
                    Thread.sleep(ms);
                } catch (Exception e) {
                    logger.error("Error spacing out API Requests", e);
                }
            }
        } catch (Exception e) {
            logger.error("Exception running TwitterScraper", e);
        }
    }

    @Benchmark(paramName = true, limit = 1000)
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
        // This is a new tweet. Run analysis on it
        final long newTweets = tweets.parallelStream()
                .map(Transforms::convert)
                .filter(tweet -> db.upsert(tweet, queryName))
                .peek(tweet -> async(() ->
                        db.upsert(analyzer.analyze(tweet).getTweet(), queryName)))
                .count();
        if (newTweets > 0)
            logger.info("Query {} returned {} results, {} of which were new",
                    queryName,
                    tweets.size(),
                    newTweets);
    }

    @Benchmark(limit = 10)
    void reconfigure() {
        Elective.ofNullable(getConfig())
                .ifPresent(config -> {
                    services.removeAll(Sets.newHashSet(updateService, analysisService));
                    if (config.runUpdater) services.add(updateService);
                    if (config.getAnalysis().run) services.add(analysisService);
                    setQueryList(config.convertQueries());
                    services.forEach(service -> service.setConfig(config));
                })
                .orElse(() -> logger.error("Could not load config"));
    }

    TwitterScraper setQueryList(final List<com.twitterscraper.model.Query> queries) {
        this.queries.clear();
        this.queries.addAll(queries);
        services.forEach(service ->
                service.setQueries(new ArrayList<>(this.queries)));
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
