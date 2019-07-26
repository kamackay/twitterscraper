package com.twitterscraper;

import com.google.inject.Guice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point class
 */
class ScraperMain {

  private static Logger logger = LoggerFactory.getLogger(ScraperMain.class);

  /**
   * Start up the Scraper Server
   */
  static void start() {
    try {
      Guice.createInjector(new MainModule()).getInstance(TwitterScraper.class).start();
    } catch (Exception e) {
      logger.error("Error Running TwitterScraper!", e);
    }
  }
}
