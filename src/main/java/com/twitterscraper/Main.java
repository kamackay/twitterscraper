package com.twitterscraper;

import com.twitterscraper.scraper.ScraperMain;
import com.twitterscraper.server.ServerMain;
import com.twitterscraper.utils.Elective;

public class Main {
  public static void main(String[] args) {
    final Elective<String> mode = Elective.ofNullable(System.getenv("MODE"));
    switch (mode.map(String::toLowerCase).orElse("scraper")) {
      default:
      case "scraper":
        ScraperMain.start();
        break;
      case "server":
        ServerMain.start();
        break;
    }
  }
}
