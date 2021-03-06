package com.twitterscraper.db;

import com.twitterscraper.utils.StringMaker;
import com.twitterscraper.utils.TweetPrinter;
import com.twitterscraper.utils.Utils;
import lombok.val;
import org.bson.Document;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

import java.util.Arrays;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class Transforms {

  public static final String ID = "id";
  static final String RETWEET_COUNT = "retweetCount";
  static final String FAVORITE_COUNT = "favoriteCount";
  private final static long DAY = 86400000;
  private final static long HOUR = 3600000;
  private final static long MINUTE = 60000;
  private final static long SECOND = 1000;

  static Document convert(final Status tweet) {
    final Document document = new Document(ID, tweet.getId())
        .append("text", tweet.getText())
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
                .map(Transforms::convert)
                .collect(toList()))
        .append("mediaEntities",
            Arrays.stream(tweet.getMediaEntities())
                .map(Transforms::convert)
                .collect(toList()))
        .append("urlEntities",
            Arrays.stream(tweet.getURLEntities())
                .map(Transforms::convert)
                .collect(toList()))
        .append("hashtagEntities",
            Arrays.stream(tweet.getHashtagEntities())
                .map(Transforms::convert)
                .collect(toList()))
        .append("place", convert(tweet.getPlace()))
        .append("geo", convert(tweet.getGeoLocation()))
        .append("tweetURL", new TweetPrinter(tweet).getTweetUrl());

    Optional.ofNullable(tweet.getUser()).ifPresent(user -> {
      document.append("user", convert(user));
      document.append("username", user.getScreenName());
    });
    return document;
  }

  private static Document convert(Place place) {
    if(place == null) return null;
    return new Document("id", place.getId())
        .append("name", place.getName())
        .append("url", place.getURL())
        .append("placeType", place.getPlaceType())
        .append("country", place.getCountry())
        .append("fullName", place.getFullName())
        .append("streetAddress", place.getStreetAddress());
  }

  private static Document convert(final GeoLocation location) {
    if(location == null) {
      return null;
    }
    return new Document("latitude", location.getLatitude())
        .append("longitude", location.getLongitude());
  }

  private static Document convert(final URLEntity e) {
    if(e == null) return null;
    return new Document("expandedURL", e.getExpandedURL())
        .append("text", e.getText())
        .append("displayURL", e.getDisplayURL())
        .append("end", e.getEnd())
        .append("start", e.getStart())
        .append("url", e.getURL());
  }

  private static Document convert(final HashtagEntity e) {
    if(e == null) return null;
    return new Document("text", e.getText())
        .append("start", e.getStart())
        .append("end", e.getEnd());
  }

  private static Document convert(final UserMentionEntity e) {
    if(e == null) return null;
    return new Document("id", e.getId())
        .append("screenName", e.getScreenName())
        .append("text", e.getText())
        .append("start", e.getStart())
        .append("end", e.getEnd())
        .append("start", e.getStart())
        .append("name", e.getName());
  }

  private static Document convert(final MediaEntity e) {
    if(e == null) return null;
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
    if(u == null) return null;
    return new Document("id", u.getId())
        .append("screenName", u.getScreenName())
        .append("name", u.getName())
        .append("createdAt", u.getCreatedAt())
        .append("followersCount", u.getFollowersCount())
        .append("friendsCount", u.getFriendsCount())
        .append("isVerified", u.isVerified());
  }

  public static String millisToReadableTime(long millis) {
    final StringMaker maker = new StringMaker();
    millis = readableTimeHelper(millis, DAY, "days", maker);
    millis = readableTimeHelper(millis, HOUR, "hours", maker);
    millis = readableTimeHelper(millis, MINUTE, "minutes", maker);
    millis = readableTimeHelper(millis, SECOND, "seconds", maker);
    readableTimeHelper(millis, 1, "ms", maker);
    if(maker.isEmpty()) return "0 ms";
    return maker.toString().trim();
  }

  private static long readableTimeHelper(long time, long unit, String unitName, StringMaker builder) {
    if(time >= unit) {
      builder.append(String.format("%d %s ", time / unit, unitName));
      return time % unit;
    } else return time;
  }
}
