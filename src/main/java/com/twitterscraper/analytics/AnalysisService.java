package com.twitterscraper.analytics;

import com.google.inject.Inject;
import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.monitors.AbstractService;
import com.twitterscraper.utils.Elective;
import com.twitterscraper.utils.benchmark.Benchmark;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.twitterscraper.db.Transforms.ANALYSIS;
import static com.twitterscraper.db.Transforms.millisToReadableTime;
import static com.twitterscraper.utils.GeneralUtils.async;
import static com.twitterscraper.utils.GeneralUtils.toPercentString;

public class AnalysisService extends AbstractService {

    protected final Logger logger = LoggerFactory.getLogger(AnalysisService.class);
    private final SentimentAnalyzer analyzer;

    @Inject
    public AnalysisService(final DatabaseWrapper db,
                           final SentimentAnalyzer analyzer) {
        super(db);
        this.analyzer = analyzer;
    }

    protected void handleQuery(final String name) {
        final long timeToRun = config.getAnalysis().timeToRun;
        List<Long> ids = db.getTweetsToAnalyze(name);
        final long startTime = System.currentTimeMillis();
        AtomicInteger numAnalyzed = new AtomicInteger();
        for (long id : ids) {
            // Stop this if it has been running too long
            if (System.currentTimeMillis() - startTime > timeToRun) break;
            if (analyzeTweet(name, id)) numAnalyzed.getAndIncrement();
        }
        int a = numAnalyzed.get();
        if (a > 0) logger.info("Analyzed {} tweets for {} in {}",
                a, name,
                millisToReadableTime(System.currentTimeMillis() - startTime));

        if (config.getAnalysis().countProgress)
            async(() -> logPercentageAnalyzed(name, db.getAllIds(name, false)));
    }

    @Benchmark(paramName = true)
    void logPercentageAnalyzed(final String name, final List<Long> ids) {
        AtomicInteger numAnalyzed = new AtomicInteger();
        for (long id : ids) {
            db.getById(name, id).ifPresent(tweet -> {
                if (tweet.get(ANALYSIS) != null) numAnalyzed.getAndIncrement();
            });
        }
        final long count = ids.size();
        logger.info("{} - {} analyzed of {} - {}",
                name, numAnalyzed.get(), count,
                toPercentString((double) numAnalyzed.get() / count));
    }

    @Benchmark(logAllParams = true, limit = 1000)
    boolean analyzeTweet(final String name, final long id) {
        Elective<Document> safeTweet = db.getById(name, id);
        if (!safeTweet.isPresent()) return false;
        final Document tweet = safeTweet.get();
        SentimentAnalyzerImpl.AnalysisResult result = analyzer.analyze(tweet);
        db.upsert(result.getTweet(), name);
        return result.wasSuccessful();
    }
}
