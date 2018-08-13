package com.twitterscraper.monitors;

import com.google.inject.Inject;
import com.twitterscraper.model.Query;
import com.twitterscraper.utils.Elective;
import com.twitterscraper.utils.benchmark.Benchmark;
import twitter4j.Status;

import java.util.ArrayList;

import static com.twitterscraper.db.DatabaseWrapper.db;
import static com.twitterscraper.twitter.TwitterWrapper.twitter;

/**
 * This is more of an example of a monitor, and not actually all that useful
 */
public class UpdateMonitor extends AbstractMonitor {

    @Inject
    public UpdateMonitor() {
        super();
    }

    @Override
    public void run() {
        new ArrayList<>(queries).parallelStream()
                .map(Query::getName)
                .forEach(this::handleQuery);
    }

    @Benchmark(paramName = true)
    void handleQuery(final String name) {
        final long id = db().getMostRetweets(name);
        final Elective<Status> safeTweet = twitter().getTweetSafe(id);
        safeTweet.ifPresent(tweet -> {
            db().upsert(tweet, name);
            logger.info("Updated ID {} for Query {}",
                    id, name);
        });
    }
}
