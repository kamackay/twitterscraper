package com.twitterscraper.db;

import com.twitterscraper.twitter.utils.TweetPrinter;
import org.bson.Document;
import twitter4j.*;

import java.util.Arrays;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class Transforms {
    static Document convert(final Status tweet) {
        final Document document = new Document("id", tweet.getId())
                .append("text", tweet.getText())
                .append("retweetCount", tweet.getRetweetCount())
                .append("createdAt", tweet.getCreatedAt())
                .append("favoriteCount", tweet.getFavoriteCount())
                //.append("contributors", tweet.getContributors())
                .append("lang", tweet.getLang())
                .append("userMentionedEntities",
                        Arrays.stream(tweet.getUserMentionEntities())
                                .map(Transforms::convert).collect(toList()))
                .append("mediaEntities",
                        Arrays.stream(tweet.getMediaEntities())
                                .map(Transforms::convert).collect(toList()))
                .append("urlEntities",
                        Arrays.stream(tweet.getURLEntities())
                                .map(Transforms::convert).collect(toList()))
                .append("hashtagEntities",
                        Arrays.stream(tweet.getHashtagEntities())
                                .map(Transforms::convert).collect(toList()))
                .append("tweetURL", new TweetPrinter(tweet).getTweetUrl());

        Optional.ofNullable(tweet.getUser()).ifPresent(user -> {
            document.append("user", convert(user));
            document.append("username", user.getScreenName());
        });
        return document;
    }

    private static Document convert(final URLEntity e) {
        return new Document("expandedURL", e.getExpandedURL())
                .append("text", e.getText())
                .append("displayURL", e.getDisplayURL())
                .append("end", e.getEnd())
                .append("start", e.getStart())
                .append("url", e.getURL());
    }

    private static Document convert(final HashtagEntity e) {
        return new Document("text", e.getText())
                .append("start", e.getStart())
                .append("end", e.getEnd());
    }

    private static Document convert(final UserMentionEntity e) {
        return new Document("id", e.getId())
                .append("screenName", e.getScreenName())
                .append("text", e.getText())
                .append("start", e.getStart())
                .append("end", e.getEnd())
                .append("start", e.getStart())
                .append("name", e.getName());
    }

    private static Document convert(final MediaEntity e) {
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

    private static Document convert(final User u) {
        return new Document("id", u.getId())
                .append("screenName", u.getScreenName())
                .append("name", u.getName())
                .append("createdAt", u.getCreatedAt())
                .append("followersCount", u.getFollowersCount())
                .append("friendsCount", u.getFriendsCount())
                .append("isVerified", u.isVerified());
    }

    private final static long HOUR = 3600000;
    private final static long MINUTE = 60000;
    private final static long SECOND = 1000;

    public static String millisToReadableTime(long millis) {
        final StringBuilder builder = new StringBuilder();
        millis = readableTimeHelper(millis, HOUR * 24, "days", builder);
        millis = readableTimeHelper(millis, HOUR, "hours", builder);
        millis = readableTimeHelper(millis, MINUTE, "minutes", builder);
        millis = readableTimeHelper(millis, SECOND, "seconds", builder);
        readableTimeHelper(millis, 1, "ms", builder);
        return builder.toString().trim();
    }

    private static long readableTimeHelper(long time, long unit, String unitName, StringBuilder builder) {
        if (time >= unit) {
            builder.append(String.format("%d %s ", time / unit, unitName));
            return time % unit;
        } else return time;
    }
}
