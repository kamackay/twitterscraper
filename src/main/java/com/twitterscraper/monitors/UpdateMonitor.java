package com.twitterscraper.monitors;

import com.google.inject.Inject;
import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.utils.Elective;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;

import java.util.List;
import java.util.Random;

import static com.twitterscraper.twitter.TwitterWrapper.twitter;

/**
 * This is more of an example of a monitor, and not actually all that useful
 */
public class UpdateMonitor extends AbstractMonitor {

    protected final Logger logger = LoggerFactory.getLogger(UpdateMonitor.class);

    @Inject
    public UpdateMonitor(final DatabaseWrapper db) {
        super(db);
    }

    protected void handleQuery(final String name) {
        List<Long> ids = db.getAllIds(name);
        final long id = ids.get(new Random().nextInt(ids.size()));
        final Elective<Status> safeTweet = twitter().getTweetSafe(id);
        safeTweet.ifPresent(tweet -> {
            db.upsert(tweet, name);
            logger.info("Updated ID {} for Query {}",
                    id, name);
        });
    }
}
