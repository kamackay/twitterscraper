package com.twitterscraper;

import org.slf4j.Logger;

import static com.twitterscraper.utils.Utils.getLogger;

public abstract class Component {

  private Logger logger = getLogger(getClass());

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
