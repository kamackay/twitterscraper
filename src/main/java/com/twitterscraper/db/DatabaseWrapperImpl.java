package com.twitterscraper.db;

import com.google.inject.Inject;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.twitterscraper.utils.Elective;
import com.twitterscraper.utils.benchmark.Benchmark;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.twitterscraper.db.Transforms.*;

public class DatabaseWrapperImpl implements DatabaseWrapper {

  private static final long DEFAULT_LONG = -1;
  private Logger logger = LoggerFactory.getLogger(getClass());

  private final MongoDatabase db;

  private Map<String, MongoCollection<Document>> collectionCache;

  @Inject
  DatabaseWrapperImpl() {
    MongoClient client = MongoClients.create(MongoClientSettings.builder()
        .applyToClusterSettings(builder ->
            builder.hosts(Collections.singletonList(
                new ServerAddress("mongodb", 27017))))
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
   * @return boolean of whether the tweet was new
   */
  public boolean upsert(final Status tweet, final String collectionName) {
    return this.getCollection(collectionName)
        .updateOne(eq("id", tweet.getId()),
            new Document("$set", convert(tweet)),
            new UpdateOptions().upsert(true)).getMatchedCount() == 0;
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

  @Benchmark(paramName = true, limit = 10)
  public long getMostRetweets(final String collectionName) {
    return Elective.ofNullable(this.getCollection(collectionName)
        .find()
        .sort(new Document(RETWEET_COUNT, -1))
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
  public long sizeInBytes(String collectionName) {
    return this.db.runCommand(new Document("collStats", collectionName))
        .getInteger("storageSize");
  }

  public long count(final String collectionName) {
    return db.getCollection(collectionName).countDocuments();
  }
}
