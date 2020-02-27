package com.twitterscraper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.utils.Elective;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import twitter4j.QueryResult;
import twitter4j.Status;

import java.util.List;

import static com.twitterscraper.twitter.TwitterWrapper.twitter;

@Slf4j
@lombok.Data
@lombok.Builder(toBuilder = true, builderClassName = "Builder", access = AccessLevel.PUBLIC)
@lombok.NoArgsConstructor(force = true, access = AccessLevel.PUBLIC)
@lombok.AllArgsConstructor(access = AccessLevel.PUBLIC)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Query implements ICloneable<Query> {
  private twitter4j.Query query;
  private QueryModel model;

  public String getName() {
    return Elective.of(model)
        .map(QueryModel::getQueryName)
        .orElse(null);
  }

  @Override
  public Query copy() {
    return new Query(new twitter4j.Query(query.getQuery()),
        model);
  }

  public void handleQuery(final DatabaseWrapper db) {
    final String queryName = this.getName();
    try {
      db.verifyIndex(queryName);
      if (!this.getModel().isUpdateExisting())
        this.getQuery().sinceId(db.getMostRecent(queryName));

      final Elective<QueryResult> safeResult = twitter().searchSafe(this.getQuery());
      if (!safeResult.isPresent()) {
        log.error("Error fetching results for Query " + queryName);
        return;
      }
      safeResult.ifPresent(result -> this.handleResult(result, db));
    } catch (Exception e) {
      log.error("Error handling query " + queryName, e);
    }
  }

  public StatusCheck status(final DatabaseWrapper db) {
    return StatusCheck.builder()
        .name(this.getName())
        .numDocuments(db.count(this.getName()))
        .numBytes(db.sizeInBytes(this.getName()))
        .build();
  }

  private void handleResult(final QueryResult result, final DatabaseWrapper db) {
    final List<Status> tweets = result.getTweets();
    final long newTweets = tweets.parallelStream()
        .filter(tweet -> db.upsert(tweet, this.getName()) > 0)
        .count();
    if (newTweets > 0)
      log.info("Query {} returned {} results, {} of which were new",
          this.getName(),
          tweets.size(),
          newTweets);
  }

  @lombok.Data
  @lombok.Builder(toBuilder = true, builderClassName = "Builder", access = AccessLevel.PUBLIC)
  @lombok.NoArgsConstructor(force = true, access = AccessLevel.PUBLIC)
  @lombok.AllArgsConstructor(access = AccessLevel.PUBLIC)
  public static class StatusCheck {
    String name;
    long numDocuments;
    long numBytes;
  }
}
