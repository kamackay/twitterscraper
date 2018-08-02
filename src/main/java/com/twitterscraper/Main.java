package com.twitterscraper;

import com.google.inject.Guice;
import com.twitterscraper.utils.benchmark.MainModule;
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
            Guice.createInjector(new MainModule()).getInstance(TwitterScraper.class).start();
        } catch (Exception e) {
            logger.error("Error Running TwitterScraper!", e);
        }
    }
}
