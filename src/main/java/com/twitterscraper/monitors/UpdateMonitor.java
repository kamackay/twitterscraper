package com.twitterscraper.monitors;

import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.model.Query;
import twitter4j.Status;
import twitter4j.Twitter;

import static com.twitterscraper.RateLimit.STATUSES_SHOW;

/**
 * This is more of an example of a monitor, and not actually all that useful
 */
public class UpdateMonitor extends AbstractMonitor {

    public UpdateMonitor(
            final Twitter twitter,
            final DatabaseWrapper db) {
        super(twitter, db);
    }

    @Override
    void run() {
        waitOnLimitSafe(STATUSES_SHOW, queries.size());
        queries.parallelStream().forEach(this::handleQuery);
    }

    private void handleQuery(final Query query) {
        try {
            long id = db.getMostRecent(query.getName());
            final Status tweet = twitter.showStatus(id);
            db.upsert(tweet, query.getName());
            logger.info("Updated ID {} for Query {}",
                    id, query.getName());
        } catch (Exception e) {
            logger.error("Error Handling Query", e);
        }
    }

    @Override
    int getFrequency() {
        return 1;
    }
}
