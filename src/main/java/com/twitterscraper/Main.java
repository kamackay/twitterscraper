package com.twitterscraper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point class
 */
public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * Main
     *
     * @param args - args
     */
    public static void main(String[] args) {
        try {
            new TwitterScraper()
                    .start();
        } catch (Exception e) {
            logger.error("Error Running TwitterScraper!", e);
        }
    }
}
