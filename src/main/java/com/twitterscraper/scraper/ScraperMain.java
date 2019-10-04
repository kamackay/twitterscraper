package com.twitterscraper.scraper;

import com.google.inject.Guice;
import com.twitterscraper.MainModule;
import org.slf4j.Logger;

import static com.twitterscraper.utils.Utils.getLogger;

/**
 * Entry point class
 */
public class ScraperMain {

  private static Logger logger = getLogger(ScraperMain.class);

  /**
   * Start up the Scraper Server
   */
  public static void start() {
    try {
      Guice.createInjector(new MainModule()).getInstance(TwitterScraper.class).start();
    } catch (Exception e) {
      logger.error("Error Running TwitterScraper!", e);
    }
  }
}
