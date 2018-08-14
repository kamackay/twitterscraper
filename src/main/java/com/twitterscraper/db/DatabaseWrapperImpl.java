package com.twitterscraper.db;

import com.google.inject.Inject;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.sun.istack.internal.NotNull;
import com.twitterscraper.utils.Elective;
import com.twitterscraper.utils.benchmark.Benchmark;
import org.bson.Document;
import twitter4j.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.*;
import static com.twitterscraper.db.Transforms.*;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class DatabaseWrapperImpl implements DatabaseWrapper {

    private static final long DEFAULT_LONG = -1;

    private final MongoDatabase db;

    //private static DatabaseWrapper instance = null;

    @Inject
    DatabaseWrapperImpl() {
        MongoClient client = MongoClients.create(MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Arrays.asList(
                                new ServerAddress("localhost", 27017)
                        )))
                .build());
        db = client.getDatabase("TwitterScraper");
    }

    /**
     * Upsert (Update or Insert) this tweet into the mongo collection provided
     *
     * @param tweet          - The tweet to add to the database
     * @param collectionName - The collection to add the tweet to
     * @return boolean of whether the tweet was new
     */
    public boolean upsert(final Status tweet, @NotNull final String collectionName) {
        return db.getCollection(collectionName)
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
            db.getCollection(collectionName)
                    .createIndex(new Document(ID, 1), new IndexOptions().unique(true));
        } catch (Exception e) {
            // ?
        }
    }

    /**
     * Get the ID of the most recent tweet in this collection
     *
     * @param collectionName - Name of the collection to query
     * @return - The ID of the most recent tweet in this collection
     */
    public long getMostRecent(@NotNull final String collectionName) {
        return Elective.ofNullable(db.getCollection(collectionName)
                .find()
                .sort(new Document(ID, -1))
                .first())
                .map(d -> d.getLong(ID))
                .orElse(DEFAULT_LONG);
    }

    @Benchmark(paramName = true, limit = 10)
    public long getMostRetweets(@NotNull final String collectionName) {
        return Elective.ofNullable(db.getCollection(collectionName)
                .find()
                .sort(new Document(RETWEET_COUNT, -1))
                .first())
                .map(d -> d.getLong(ID))
                .orElse(DEFAULT_LONG);
    }

    public List<Long> getAllIds(final String collectionName) {
        return getAllIds(collectionName, true);
    }

    @Benchmark(paramName = true, limit = 500)
    public List<Long> getAllIds(final String collectionName, final boolean sort) {
        return Elective.ofNullable(db.getCollection(collectionName))
                .map(MongoCollection::find)
                .map(d -> d.projection(fields(include(ID), excludeId())))
                .map(d -> sort ? d.sort(new Document(ID, 1)) : d)
                .map(d -> d.into(new ArrayList<>()))
                .map(d -> d.stream()
                        .map(doc -> doc.getLong(ID))
                        .collect(toList()))
                .orElse(emptyList());
    }

    @Override
    @Benchmark(paramName = true, limit = 10)
    public Elective<Document> getById(final String collectionName, final long id) {
        return Elective.ofNullable(db.getCollection(collectionName))
                .map(MongoCollection::find)
                .map(d -> d.filter(new Document(ID, id)))
                .map(FindIterable::first);
    }
}
