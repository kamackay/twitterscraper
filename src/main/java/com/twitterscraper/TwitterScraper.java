package com.twitterscraper;

import com.google.inject.Inject;
import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.model.Config;
import com.twitterscraper.model.Query;
import com.twitterscraper.monitors.AbstractMonitor;
import com.twitterscraper.monitors.UpdateMonitor;
import com.twitterscraper.utils.Elective;
import com.twitterscraper.utils.benchmark.Benchmark;
import org.slf4j.LoggerFactory;
import twitter4j.QueryResult;
import twitter4j.Status;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.twitterscraper.db.Transforms.millisToReadableTime;
import static com.twitterscraper.twitter.TwitterWrapper.getWaitTimeForQueries;
import static com.twitterscraper.twitter.TwitterWrapper.twitter;
import static com.twitterscraper.utils.Utils.formatBytes;


public class TwitterScraper extends Component {

  private final List<com.twitterscraper.model.Query> queries;
  private final Set<AbstractMonitor> monitors;
  private final UpdateMonitor updateMonitor;
  private final DatabaseWrapper db;
  private org.slf4j.Logger logger = LoggerFactory.getLogger(TwitterScraper.class);

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
    queries.parallelStream().forEach(query -> handleQuery(query.getName(), query));

    monitors.forEach(AbstractMonitor::run);

    this.logDatabaseSize();

    try {
      final long ms = getWaitTimeForQueries(queries.size());
      logger.info("Waiting for {} to span out API requests", millisToReadableTime(ms));
      Thread.sleep(ms);
    } catch (Exception e) {
      logger.error("Error spacing out API Requests", e);
    }
  }

  @Benchmark(paramName = true, limit = 1000)
  void handleQuery(final String queryName, final Query query) {
    try {
      db.verifyIndex(queryName);
      if (!query.getModel().isUpdateExisting())
        query.getQuery().sinceId(db.getMostRecent(queryName));

      final Elective<QueryResult> safeResult = twitter().searchSafe(query.getQuery());
      if (!safeResult.isPresent()) {
        logger.error("Error fetching results for Query " + queryName);
        return;
      }
      safeResult.ifPresent(result -> this.handleResult(result, queryName));

      logger.info("{} documents in the '{}' collection, {}", db.count(queryName), queryName,
          formatBytes(db.sizeInBytes(queryName)));
    } catch (Exception e) {
      logger.error("Error handling query " + queryName, e);
    }
  }

  private void logDatabaseSize() {
    // Print the full size of the database, for record-keeping
    final long totalSize = queries.stream().
        map(Query::getName)
        .mapToLong(this.db::sizeInBytes)
        .sum();
    logger.info("Total Database size is {}", formatBytes(totalSize));
  }

  private void handleResult(final QueryResult result, final String queryName) {
    final List<Status> tweets = result.getTweets();
    final long newTweets = tweets.parallelStream()
        .filter(tweet -> db.upsert(tweet, queryName))
        .count();
    if (newTweets > 0)
      logger.info("Query {} returned {} results, {} of which were new",
          queryName,
          tweets.size(),
          newTweets);
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
