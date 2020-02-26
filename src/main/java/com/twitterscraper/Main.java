package com.twitterscraper;

import com.twitterscraper.scraper.ScraperMain;
import com.twitterscraper.server.ServerMain;
import com.twitterscraper.utils.Elective;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
  public static void main(String[] args) {
    final Elective<String> mode = Elective.ofNullable(System.getenv("MODE"));
    switch (mode.map(String::toLowerCase).orElse("scraper")) {
      default:
      case "scraper":
        log.info("Starting Scraper Mode");
        ScraperMain.start();
        break;
      case "server":
        ServerMain.start();
        break;
    }
  }
}
