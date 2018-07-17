package com.twitterscraper.model;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import twitter4j.Status;

import java.util.Arrays;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

public class DatabaseWrapper {
    private final MongoClient client;
    private final MongoCollection<Document> db;

    public DatabaseWrapper() {
        client = MongoClients.create(MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Arrays.asList(
                                new ServerAddress("localhost", 27017)
                        )))
                .build());
        db = client.getDatabase("tweets").getCollection("tweets");
    }

    public void upsert(final Status tweet) {
        db.updateOne(eq("id", tweet.getId()),
                new Document("$set", convert(tweet)),
                new UpdateOptions().upsert(true));
    }

    private Document convert(final Status tweet) {
        final Document document = new Document("id", tweet.getId())
                .append("text", tweet.getText())
                .append("retweetCount", tweet.getRetweetCount())
                .append("createdAt", tweet.getCreatedAt())
                .append("favoriteCount", tweet.getFavoriteCount());

        Optional.ofNullable(tweet.getUser()).ifPresent(user -> {
            document.append("user", new Document("id", user.getId())
                    .append("screenName", user.getScreenName())
                    .append("name", user.getName())
                    .append("friendsCount", user.getFriendsCount())
                    .append("isVerified", user.isVerified()));
            document.append("username", user.getScreenName());
        });

        return document;
    }
}
