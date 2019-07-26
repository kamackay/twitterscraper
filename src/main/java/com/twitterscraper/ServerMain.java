package com.twitterscraper;

import com.google.inject.Guice;
import com.twitterscraper.server.Server;
import com.twitterscraper.server.ServerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ServerMain {
  private static Logger logger = LoggerFactory.getLogger(ScraperMain.class);

  /**
   * Start up the Scraper Server
   */
  static void start() {
    try {
      Guice.createInjector(new ServerModule()).getInstance(Server.class).start();
    } catch (Exception e) {
      logger.error("Error Running TwitterScraper!", e);
    }
  }
}
