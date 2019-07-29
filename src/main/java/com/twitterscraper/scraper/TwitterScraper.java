package com.twitterscraper.scraper;

import com.google.inject.Inject;
import com.twitterscraper.Component;
import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.model.Config;
import com.twitterscraper.model.Query;
import com.twitterscraper.monitors.AbstractMonitor;
import com.twitterscraper.monitors.UpdateMonitor;
import com.twitterscraper.utils.Elective;
import com.twitterscraper.utils.benchmark.Benchmark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.twitterscraper.db.Transforms.millisToReadableTime;
import static com.twitterscraper.twitter.TwitterWrapper.getWaitTimeForQueries;
import static com.twitterscraper.twitter.TwitterWrapper.twitter;
import static com.twitterscraper.utils.Utils.formatBytes;
import static com.twitterscraper.utils.Utils.padString;


public class TwitterScraper extends Component {

  private final List<com.twitterscraper.model.Query> queries;
  private final Set<AbstractMonitor> monitors;
  private final UpdateMonitor updateMonitor;
  private final DatabaseWrapper db;
  private Logger logger = LoggerFactory.getLogger(TwitterScraper.class);

  @Inject
  TwitterScraper(
      final UpdateMonitor updateMonitor,
      final DatabaseWrapper db) {
    this.updateMonitor = updateMonitor;
    this.db = db;
    queries = new ArrayList<>();
    monitors = new HashSet<>();
    reconfigure();
  }

  public String name() {
    return this.getClass().getSimpleName();
  }

  public void run() {
    reconfigure();
    queries.parallelStream().forEach(query -> query.handleQuery(db));

    monitors.forEach(AbstractMonitor::run);

    twitter().logAllLimits();
    List<Query.StatusCheck> checks = queries.stream()
        .map(query -> query.status(db))
        .collect(Collectors.toList());

    final int longestName = checks.stream()
        .map(Query.StatusCheck::getName)
        .mapToInt(String::length)
        .max()
        .orElse(0);
    final int longestSize = checks.stream()
        .map(Query.StatusCheck::getNumDocuments)
        .map(String::valueOf)
        .mapToInt(String::length)
        .max().orElse(0);
    logger.info("Collection Statuses:");
    checks.forEach(check -> {
      logger.info("{}: Records: {}, Size: {}",
          padString(check.getName(), longestName),
          padString(String.valueOf(check.getNumDocuments()), longestSize),
          formatBytes(check.getNumBytes()));
    });
    this.logDatabaseSize();

    try {
      final long ms = getWaitTimeForQueries(queries.size());
      logger.info("Waiting for {} to span out API requests", millisToReadableTime(ms));
      Thread.sleep(ms);
    } catch (Exception e) {
      logger.error("Error spacing out API Requests", e);
    }
  }

  private void logDatabaseSize() {
    // Print the full size of the database, for record-keeping
    final long totalSize = queries.stream()
        .map(Query::getName)
        .mapToLong(this.db::sizeInBytes)
        .sum();
    final long totalCount = queries.stream()
        .map(Query::getName)
        .mapToLong(this.db::count)
        .sum();
    logger.info("Total Database size is {}, {} records", formatBytes(totalSize), totalCount);
  }

  @Benchmark(limit = 10)
  void reconfigure() {
    Elective.ofNullable(Config.get())
        .ifPresent(config -> {
          monitors.remove(updateMonitor);
          if (config.runUpdater) monitors.add(updateMonitor);
          setQueryList(config.convertQueries());
        })
        .orElse(() -> logger.error("Could not load config"));
  }

  TwitterScraper setQueryList(final List<com.twitterscraper.model.Query> queries) {
    this.queries.clear();
    this.queries.addAll(queries);
    monitors.forEach(abstractMonitor ->
        abstractMonitor.setQueries(new ArrayList<>(this.queries)));
    return this;
  }
}
