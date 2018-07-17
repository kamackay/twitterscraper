package com.twitterscraper.model;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.sun.istack.internal.NotNull;
import org.bson.Document;
import twitter4j.*;

import java.util.Arrays;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;
import static java.util.stream.Collectors.toList;

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

    public void upsert(final Status tweet, @NotNull final String collectionName) {
        db.getCollection(collectionName)
                .updateOne(eq("id", tweet.getId()),
                        new Document("$set", convert(tweet)),
                        new UpdateOptions().upsert(true));
    }

    private Document convert(final Status tweet) {
        final Document document = new Document("id", tweet.getId())
                .append("text", tweet.getText())
                .append("retweetCount", tweet.getRetweetCount())
                .append("createdAt", tweet.getCreatedAt())
                .append("favoriteCount", tweet.getFavoriteCount())
                .append("userMentionedEntities",
                        Arrays.stream(tweet.getUserMentionEntities())
                                .map(this::convert).collect(toList()))
                .append("mediaEntities",
                        Arrays.stream(tweet.getMediaEntities())
                                .map(this::convert).collect(toList()))
                .append("urlEntities",
                        Arrays.stream(tweet.getURLEntities())
                                .map(this::convert).collect(toList()))
                .append("hashtagEntities",
                        Arrays.stream(tweet.getHashtagEntities())
                                .map(this::convert).collect(toList()));

        Optional.ofNullable(tweet.getUser()).ifPresent(user -> {
            document.append("user", convert(user));
            document.append("username", user.getScreenName());
        });
        return document;
    }

    private Document convert(final URLEntity e) {
        return new Document("expandedURL", e.getExpandedURL())
                .append("text", e.getText())
                .append("displayURL", e.getDisplayURL())
                .append("end", e.getEnd())
                .append("start", e.getStart())
                .append("url", e.getURL());
    }

    private Document convert(final HashtagEntity e) {
        return new Document("text", e.getText())
                .append("start", e.getStart())
                .append("end", e.getEnd());
    }

    private Document convert(final UserMentionEntity e) {
        return new Document("id", e.getId())
                .append("screenName", e.getScreenName())
                .append("text", e.getText())
                .append("start", e.getStart())
                .append("end", e.getEnd())
                .append("start", e.getStart())
                .append("name", e.getName());
    }

    private Document convert(final MediaEntity e) {
        return new Document("id", e.getId())
                .append("mediaURL", e.getMediaURL())
                .append("mediaURLHttps", e.getMediaURLHttps())
                .append("expandedULR", e.getExpandedURL())
                .append("type", e.getType())
                .append("start", e.getStart())
                .append("end", e.getEnd())
                .append("displayURL", e.getDisplayURL())
                .append("url", e.getURL());
    }

    private Document convert(final User u) {
        return new Document("id", u.getId())
                .append("screenName", u.getScreenName())
                .append("name", u.getName())
                .append("createdAt", u.getCreatedAt())
                .append("followersCount", u.getFollowersCount())
                .append("friendsCount", u.getFriendsCount())
                .append("isVerified", u.isVerified());
    }
}
