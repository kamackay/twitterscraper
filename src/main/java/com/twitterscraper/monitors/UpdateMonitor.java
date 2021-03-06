package com.twitterscraper.monitors;

import com.google.inject.Inject;
import com.mongodb.client.model.Aggregates;
import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.model.Config;
import com.twitterscraper.model.Query;
import com.twitterscraper.twitter.TwitterWrapper;
import com.twitterscraper.utils.benchmark.Benchmark;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;

import static com.twitterscraper.db.Transforms.ID;
import static com.twitterscraper.twitter.RateLimit.STATUSES_SHOW;
import static com.twitterscraper.twitter.TwitterWrapper.twitter;

@Slf4j
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
    final Config config = Config.get();
    final int numberToUpdate = twitter()
        .getLimit(STATUSES_SHOW.getName())
        .map(limit -> {
          if (config != null &&
              limit.getSecondsUntilReset() * 1000 <=
                  TwitterWrapper.getWaitTimeForQueries(config.queries.size())) {
            final int n = limit.getRemaining() / config.queries.size();
            log.info("Burn through the remaining {} requests for {}", n, name);
            return n;
          }
          return limit.getRemaining() / 100;
        })
        .orElse(2);
    db.getCollection(name)
        .aggregate(Collections.singletonList(Aggregates.sample(numberToUpdate)))
        .into(new ArrayList<>())
        .parallelStream()
        .map(document -> document.getLong(ID))
        .forEach(id -> this.update(id, name));

//    final long id = db.getMostRetweets(name);
//    this.update(id, name);
  }

  private void update(final long id, final String name) {
    twitter().getTweetSafe(id)
        .ifPresent(tweet -> {
          final int timesUpdated = db.upsert(tweet, name);
          log.debug("Updated Tweet for Query {} - has been updated {} times",
              name, timesUpdated);
        });
  }
}
