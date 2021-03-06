package com.twitterscraper.db;

import com.google.inject.Inject;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import com.twitterscraper.utils.Elective;
import com.twitterscraper.utils.benchmark.Benchmark;
import org.bson.Document;
import org.slf4j.Logger;
import twitter4j.Status;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.newArrayList;
import static com.mongodb.client.model.Filters.eq;
import static com.twitterscraper.db.Transforms.*;
import static com.twitterscraper.utils.Utils.getLogger;

public class DatabaseWrapperImpl implements DatabaseWrapper {

  private static final long DEFAULT_LONG = -1;
  private final MongoDatabase db;
  private Logger logger = getLogger(DatabaseWrapperImpl.class);
  private Map<String, MongoCollection<Document>> collectionCache;

  @Inject
  DatabaseWrapperImpl() {
    final String host = Optional.ofNullable(System.getenv("DB_HOST"))
        .orElse("twitterscraper-db");
    logger.info("Connecting to Mongo Host: {}", host);
    MongoClient client = MongoClients.create(MongoClientSettings.builder()
        .applyToClusterSettings(builder ->
            builder.hosts(Collections.singletonList(
                new ServerAddress(host, 27017))))
        .build());
    db = client.getDatabase("TwitterScraper");
    this.collectionCache = new HashMap<>();
  }

  public MongoCollection<Document> getCollection(final String name) {
    final MongoCollection<Document> collection = Elective
        .ofNullable(this.collectionCache.get(name))
        .orElseGet(() -> this.db.getCollection(name));
    this.collectionCache.putIfAbsent(name, collection);
    return collection;
  }

  /**
   * Upsert (Update or Insert) this tweet into the mongo collection provided
   *
   * @param tweet          - The tweet to add to the database
   * @param collectionName - The collection to add the tweet to
   * @return Number of times this tweet has been updated
   */
  public int upsert(final Status tweet, final String collectionName) {
    int timesUpdated = Elective.ofNullable(this.getCollection(collectionName)
        .find(eq("id", tweet.getId()))
        .first())
        .map(d -> d.getInteger("timesUpdated"))
        .orElse(-1);
    this.getCollection(collectionName)
        .updateOne(eq("id", tweet.getId()),
            new Document("$set", convert(tweet)
                .append("timesUpdated", ++timesUpdated)),
            new UpdateOptions().upsert(true));
    return timesUpdated;
  }

  /**
   * Verify that the ID column on this collection has a unique constraint
   *
   * @param collectionName - Name of the collection to add an index to
   */
  public void verifyIndex(final String collectionName) {
    try {
      this.getCollection(collectionName)
          .createIndex(new Document(ID, 1), new IndexOptions().unique(true));
    } catch (Exception e) {
      logger.error("Error while verifying index", e);
    }
  }

  /**
   * Get the ID of the most recent tweet in this collection
   *
   * @param collectionName - Name of the collection to query
   * @return - The ID of the most recent tweet in this collection
   */
  public long getMostRecent(final String collectionName) {
    return Elective.ofNullable(this.getCollection(collectionName)
        .find()
        .sort(new Document(ID, -1))
        .first())
        .map(d -> d.getLong(ID))
        .orElse(DEFAULT_LONG);
  }

  @Benchmark(paramName = true, limit = 1)
  public long getMostRetweets(final String collectionName) {
    return Elective.ofNullable(this.getCollection(collectionName)
        .aggregate(Collections.singletonList(
            Aggregates.group(null,
                Accumulators.max(RETWEET_COUNT, 1))))
        .first())
        .map(d -> d.getLong(ID))
        .orElse(DEFAULT_LONG);
  }

  @Override
  public Collection<Document> getAll(final String collectionName) {
    return this.getAll(collectionName, new Document());
  }

  @Override
  public Collection<Document> getAll(String collectionName, Document filter) {
    return this.getCollection(collectionName)
        .find(filter)
        .limit(1000) // For the sake of protecting the Java Heap
        .into(new ArrayList<>());
  }

  @Override
  @Benchmark(paramName = true, limit = 10)
  public long sizeInBytes(String collectionName) {
    return this.db.runCommand(new Document("collStats", collectionName))
        .getInteger("storageSize");
  }

  @Benchmark(paramName = true, limit = 10)
  public long count(final String collectionName) {
    return db.getCollection(collectionName)
        .estimatedDocumentCount(
            new EstimatedDocumentCountOptions()
                .maxTime(2, TimeUnit.SECONDS));
  }

  @Benchmark(limit = 1000)
  public Collection<String> getCollections() {
    return db.listCollectionNames().into(newArrayList());
  }

  public void delete(final String collectionName) {
    db.getCollection(collectionName).drop();
  }
}
