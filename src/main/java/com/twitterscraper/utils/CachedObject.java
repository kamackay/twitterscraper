package com.twitterscraper.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Slf4j
public class CachedObject<T> {

  private final Timer timer;
  private final AtomicReference<T> value;

  private CachedObject(final Supplier<T> fetcher,
                       final long updateInterval,
                       final String name) {
    timer = new Timer();

    this.value = new AtomicReference<>();

    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        log.info("Running Update on CachedObject<{}>", name);
        value.set(fetcher.get());
      }
    }, 0, updateInterval);
  }

  public T getCurrent() {
    return this.value.get();
  }

  public void cancel() {
    this.timer.cancel();
  }

  public static <T> CachedObject<T> from(final Supplier<T> fetcher, final Class<T> clazz) {
    return from(fetcher, 1000, clazz);
  }

  public static <T> CachedObject<T> from(final Supplier<T> fetcher, final long interval, final Class<T> clazz) {
    return new CachedObject<>(fetcher, interval, clazz.getSimpleName());
  }
}
