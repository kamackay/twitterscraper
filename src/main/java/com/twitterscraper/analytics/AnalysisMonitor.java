package com.twitterscraper.analytics;

import com.google.inject.Inject;
import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.monitors.AbstractMonitor;
import com.twitterscraper.utils.benchmark.Benchmark;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.twitterscraper.db.Transforms.millisToReadableTime;
import static com.twitterscraper.utils.GeneralUtils.*;

public class AnalysisMonitor extends AbstractMonitor {

    private static final String ANALYSIS_NAME = "analysis";

    protected final Logger logger = LoggerFactory.getLogger(AnalysisMonitor.class);


    @Inject
    public AnalysisMonitor(final DatabaseWrapper db) {
        super(db);
    }

    protected void handleQuery(final String name) {
        final long timeToRun = config.getAnalysis().timeToRun;
        List<Long> ids = db.getAllIds(name, true);
        final long startTime = System.currentTimeMillis();
        AtomicInteger numAnalyzed = new AtomicInteger();
        for (long id : ids) {
            // Stop this if it has been running too long
            if (System.currentTimeMillis() - startTime > timeToRun) break;
            db.getById(name, id).ifPresent(tweet -> {
                if (analyzeTweet(name, tweet)) numAnalyzed.getAndIncrement();
            });
        }
        int a = numAnalyzed.get();
        if (a > 0) logger.info("Analyzed {} tweets for {} in {}",
                a, name,
                millisToReadableTime(System.currentTimeMillis() - startTime));

        if (config.getAnalysis().countProgress)
            async(() -> getPercentageAnalyzed(name, ids));
    }

    @Benchmark(paramName = true)
    void getPercentageAnalyzed(final String name, final List<Long> ids) {
        AtomicInteger numAnalyzed = new AtomicInteger();
        for (long id : ids) {
            db.getById(name, id).ifPresent(tweet -> {
                if (tweet.get(ANALYSIS_NAME) != null) numAnalyzed.getAndIncrement();
            });
        }
        final long count = ids.size();
        logger.info("{} - {} analyzed of {} - {}",
                name, numAnalyzed.get(), count,
                toPercentString((double) numAnalyzed.get() / count));
    }

    private boolean analyzeTweet(final String name, final Document tweet) {
        if (!needsAnalysis(tweet)) return false;
        safeSleep(100); // TODO do analysis
        tweet.append(ANALYSIS_NAME, new Document("time", System.currentTimeMillis()));
        db.upsert(tweet, name);
        return true;
    }

    private boolean needsAnalysis(final Document tweet) {
        return tweet.get(ANALYSIS_NAME) == null;
    }
}
