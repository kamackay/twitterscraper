package com.twitterscraper.scraper;

import com.google.inject.Guice;
import com.twitterscraper.MainModule;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import static com.twitterscraper.utils.Utils.getLogger;

/**
 * Entry point class
 */
@Slf4j
public class ScraperMain {


  /**
   * Start up the Scraper Server
   */
  public static void start() {
    try {
      Guice.createInjector(new MainModule()).getInstance(TwitterScraper.class).start();
    } catch (Exception e) {
      log.error("Error Running TwitterScraper!", e);
    }
  }
}
