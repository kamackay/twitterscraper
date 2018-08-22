package com.twitterscraper.db;

import com.twitterscraper.utils.StringMaker;
import org.bson.Document;
import twitter4j.*;

import java.util.Arrays;
import java.util.Optional;

import static com.twitterscraper.utils.TweetPrinter.getTweetUrl;
import static java.util.stream.Collectors.toList;

public class Transforms {

    public static final String ID = "id";
    public static final String TEXT = "text";
    static final String RETWEET_COUNT = "retweetCount";
    public static final String ANALYSIS = "analysis";
    static final String FAVORITE_COUNT = "favoriteCount";

    public static Document convert(final Status tweet) {
        final Document document = new Document(ID, tweet.getId())
                .append(TEXT, tweet.getText())
                .append(RETWEET_COUNT, tweet.getRetweetCount())
                .append("createdAt", tweet.getCreatedAt())
                .append(FAVORITE_COUNT, tweet.getFavoriteCount())
                .append("lang", tweet.getLang())
                .append("inReplyToUserId", tweet.getInReplyToUserId())
                .append("inReplyToScreenName", tweet.getInReplyToScreenName())
                .append("scopes", tweet.getScopes())
                .append("accessLevel", tweet.getAccessLevel())
                .append("source", tweet.getSource())
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
                .append("place", convert(tweet.getPlace()))
                .append("tweetURL", getTweetUrl(tweet));

        Optional.ofNullable(tweet.getUser()).ifPresent(user -> {
            document.append("user", convert(user));
            document.append("username", user.getScreenName());
        });
        return document;
    }

    private static Document convert(Place place) {
        if (place == null) return null;
        return new Document(ID, place.getId())
                .append("name", place.getName())
                .append("url", place.getURL())
                .append("placeType", place.getPlaceType())
                .append("country", place.getCountry())
                .append("fullName", place.getFullName())
                .append("streetAddress", place.getStreetAddress());
    }

    private static Document convert(final URLEntity e) {
        if (e == null) return null;
        return new Document("expandedURL", e.getExpandedURL())
                .append("text", e.getText())
                .append("displayURL", e.getDisplayURL())
                .append("end", e.getEnd())
                .append("start", e.getStart())
                .append("url", e.getURL());
    }

    private static Document convert(final HashtagEntity e) {
        if (e == null) return null;
        return new Document("text", e.getText())
                .append("start", e.getStart())
                .append("end", e.getEnd());
    }

    private static Document convert(final UserMentionEntity e) {
        if (e == null) return null;
        return new Document(ID, e.getId())
                .append("screenName", e.getScreenName())
                .append("text", e.getText())
                .append("start", e.getStart())
                .append("end", e.getEnd())
                .append("start", e.getStart())
                .append("name", e.getName());
    }

    private static Document convert(final MediaEntity e) {
        if (e == null) return null;
        return new Document(ID, e.getId())
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
        if (u == null) return null;
        return new Document(ID, u.getId())
                .append("screenName", u.getScreenName())
                .append("name", u.getName())
                .append("createdAt", u.getCreatedAt())
                .append("followersCount", u.getFollowersCount())
                .append("friendsCount", u.getFriendsCount())
                .append("isVerified", u.isVerified());
    }

    private final static long DAY = 86400000;
    private final static long HOUR = 3600000;
    private final static long MINUTE = 60000;
    private final static long SECOND = 1000;

    public static String millisToReadableTime(long millis) {
        final StringMaker maker = new StringMaker();
        if (millis < 0) {
            millis = Math.abs(millis);
            maker.append("-");
        }
        millis = readableTimeHelper(millis, DAY, "days", maker);
        millis = readableTimeHelper(millis, HOUR, "hours", maker);
        millis = readableTimeHelper(millis, MINUTE, "minutes", maker);
        millis = readableTimeHelper(millis, SECOND, "seconds", maker);
        readableTimeHelper(millis, 1, "ms", maker);
        if (maker.isEmpty()) return "0 ms";
        return maker.toString().trim();
    }

    private static long readableTimeHelper(long time, long unit, String unitName, StringMaker builder) {
        if (time >= unit) {
            builder.append(String.format("%d %s ", time / unit, unitName));
            return time % unit;
        } else return time;
    }
}
