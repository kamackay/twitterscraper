package com.twitterscraper.monitors;

import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.model.Query;
import com.twitterscraper.utils.Elective;
import twitter4j.Status;

import java.util.ArrayList;

import static com.twitterscraper.twitter.TwitterWrapper.twitter;

/**
 * This is more of an example of a monitor, and not actually all that useful
 */
public class UpdateMonitor extends AbstractMonitor {

    public UpdateMonitor(
            final DatabaseWrapper db) {
        super(db);
    }

    @Override
    void run() {
        new ArrayList<>(queries).forEach(this::handleQuery);
    }

    private void handleQuery(final Query query) {
        final long id = db.getMostRetweets(query.getName());
        final Elective<Status> safeTweet = twitter().getTweetSafe(id);
        safeTweet.ifPresent(tweet -> {
            db.upsert(tweet, query.getName());
            logger.info("Updated ID {} for Query {}",
                    id, query.getName());
        });
    }

    @Override
    int getFrequency() {
        return 1;
    }
}
