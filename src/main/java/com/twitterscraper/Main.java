package com.twitterscraper;

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
            new TwitterScraper()
                    .setTweetHandler(TwitterScraper::printTweet)
                    .run();
        } catch (Exception e) {
            logger.e(e);
        }
    }
}
