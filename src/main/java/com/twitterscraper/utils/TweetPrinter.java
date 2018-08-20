package com.twitterscraper.utils;

import twitter4j.Status;

public class TweetPrinter {

    public static String removeEmojis(final String content) {
        final String cleanString = content.replaceAll("[^\\x00-\\x7F]", "");
        return cleanString;
    }

    public static String getTweetUrl(final Status tweet) {
        return "https://twitter.com/statuses/" + tweet.getId();
    }
}
