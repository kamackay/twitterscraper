package com.twitterscraper.utils;

import java.util.TimerTask;

public class Task extends TimerTask {

  private final Runnable runnable;

  public Task(final Runnable runnable) {
    this.runnable = runnable;
  }

  @Override
  public void run() {
    runnable.run();
  }
}
