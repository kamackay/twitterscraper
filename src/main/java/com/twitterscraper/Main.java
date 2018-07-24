package com.twitterscraper;

import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.logging.Logger;

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
                    .start();
        } catch (Exception e) {
            logger.e(e);
        }
    }
}
