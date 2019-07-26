package com.twitterscraper.monitors;

import com.google.inject.Inject;
import com.mongodb.client.model.Aggregates;
import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.model.Query;
import com.twitterscraper.utils.benchmark.Benchmark;
import twitter4j.RateLimitStatus;

import java.util.ArrayList;
import java.util.Collections;

import static com.twitterscraper.db.Transforms.ID;
import static com.twitterscraper.twitter.RateLimit.STATUSES_SHOW;
import static com.twitterscraper.twitter.TwitterWrapper.twitter;

/**
 * This is more of an example of a monitor, and not actually all that useful
 */
public class UpdateMonitor extends AbstractMonitor {

  @Inject
  public UpdateMonitor(final DatabaseWrapper db) {
    super(db);
  }

  @Override
  public void run() {
    new ArrayList<>(queries).parallelStream()
        .map(Query::getName)
        .forEach(this::handleQuery);
  }

  @Benchmark(paramName = true, limit = 1000)
  void handleQuery(final String name) {
    final int numberToUpdate = twitter()
        .getLimit(STATUSES_SHOW.getName())
        .map(RateLimitStatus::getRemaining)
        .map(limit -> limit / 100)
        .orElse(2);
    db.getCollection(name)
        .aggregate(Collections.singletonList(Aggregates.sample(numberToUpdate)))
        .into(new ArrayList<>())
        .parallelStream()
        .map(document -> document.getLong(ID))
        .forEach(id -> this.update(id, name));

    final long id = db.getMostRetweets(name);
    this.update(id, name);
  }

  private void update(final long id, final String name) {
    twitter().getTweetSafe(id)
        .ifPresent(tweet -> {
          db.upsert(tweet, name);
          logger.info("Updated ID {} for Query {}",
              id, name);
        });
  }
}
