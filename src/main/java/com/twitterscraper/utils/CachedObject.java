package com.twitterscraper.utils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class CachedObject<T> {

  private final Timer timer;
  private final AtomicReference<T> value;

  private CachedObject(final Supplier<T> fetcher, final long updateInterval) {
    timer = new Timer();

    this.value = new AtomicReference<>();

    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        value.set(fetcher.get());
      }
    }, updateInterval);
  }

  public T getCurrent() {
    return this.value.get();
  }

  public void cancel() {
    this.timer.cancel();
  }

  public static <T> CachedObject<T> from(final Supplier<T> fetcher) {
    return from(fetcher, 1000);
  }

  public static <T> CachedObject<T> from(final Supplier<T> fetcher, final long interval) {
    return new CachedObject<>(fetcher, interval);
  }
}
