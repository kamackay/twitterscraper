package com.twitterscraper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Component {

  private Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * Run the Component
   */
  public void start() {
    new Thread(() -> {
      try {
        while (true) {
          this.run();
        }
      } catch (Exception e) {
        logger.error("Exception running component {}", this.name(), e);
      }
    }).start();
  }

  public abstract String name();

  public abstract void run();
}
