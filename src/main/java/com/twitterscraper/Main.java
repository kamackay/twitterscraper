package com.twitterscraper;

import com.twitterscraper.scraper.ScraperMain;
import com.twitterscraper.server.ServerMain;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
  public static void main(String[] args) {
    log.info("Starting!");
    ServerMain.start();
    ScraperMain.start();
  }
}
