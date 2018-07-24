package com.twitterscraper.db;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.sun.istack.internal.NotNull;
import com.twitterscraper.logging.Logger;
import org.bson.Document;
import twitter4j.Status;

import java.util.Arrays;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;
import static com.twitterscraper.db.Transforms.convert;

public class DatabaseWrapper {
    private final MongoDatabase db;

    private static final Logger logger = new Logger(DatabaseWrapper.class);

    public DatabaseWrapper() {
        MongoClient client = MongoClients.create(MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Arrays.asList(
                                new ServerAddress("localhost", 27017)
                        )))
                .build());
        db = client.getDatabase("TwitterScraper");
    }

    public void upsert(final Status tweet, @NotNull final String collectionName) {
        db.getCollection(collectionName)
                .updateOne(eq("id", tweet.getId()),
                        new Document("$set", convert(tweet)),
                        new UpdateOptions().upsert(true));
    }

    public long getMostRecent(@NotNull final String collectionName) {
        return Optional.ofNullable(db.getCollection(collectionName)
                .find()
                .sort(new Document("id", -1))
                .first())
                .map(d -> d.getLong("id"))
                .orElse(-1L);
    }
}
