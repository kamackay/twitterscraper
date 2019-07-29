package com.twitterscraper.server;

import com.google.inject.Guice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMain {
  private static Logger logger = LoggerFactory.getLogger(ServerMain.class);

  /**
   * Start up the Scraper Server
   */
  public static void start() {
    try {
      Guice.createInjector(new ServerModule()).getInstance(Server.class).start();
    } catch (Exception e) {
      logger.error("Error Running TwitterScraper!", e);
    }
  }
}
