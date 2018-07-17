package com.twitterscraper;

import com.twitterscraper.logging.Logger;
import com.twitterscraper.model.DatabaseWrapper;

/**
 * Entry point class
 */
public class Main {

    private static final Logger logger = new Logger(Main.class);

    /**
     * Main
     *
     * @param args - args
     */
    public static void main(String[] args) {
        try {
            final DatabaseWrapper db = new DatabaseWrapper();
            new TwitterScraper()
                    .setTweetHandler(tweet -> {
                        TwitterScraper.printTweet(tweet);
                        db.upsert(tweet);
                    })
                    .run();
        } catch (Exception e) {
            logger.e(e);
        }
    }
}
