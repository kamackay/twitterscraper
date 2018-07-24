package com.twitterscraper.db;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.sun.istack.internal.NotNull;
import org.bson.Document;
import twitter4j.Status;

import java.util.Arrays;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;
import static com.twitterscraper.db.Transforms.ID;
import static com.twitterscraper.db.Transforms.convert;

public class DatabaseWrapper {
    private final MongoDatabase db;

    public DatabaseWrapper() {
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
        return Optional.ofNullable(db.getCollection(collectionName)
                .find()
                .sort(new Document(ID, -1))
                .first())
                .map(d -> d.getLong(ID))
                .orElse(-1L);
    }
}
