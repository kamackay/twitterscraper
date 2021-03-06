package com.twitterscraper.db;

import com.google.inject.ImplementedBy;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import twitter4j.Status;

import java.util.Collection;

@ImplementedBy(DatabaseWrapperImpl.class)
public interface DatabaseWrapper {
  long getMostRetweets(final String collectionName);

  int upsert(final Status tweet, final String name);

  void verifyIndex(final String collectionName);

  long getMostRecent(final String collectionName);

  long count(final String collectionName);

  long sizeInBytes(final String collectionName);

  Collection<Document> getAll(final String collectionName, final Document filter);

  Collection<Document> getAll(final String collectionName);

  MongoCollection<Document> getCollection(final String name);
}
