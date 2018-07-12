package com.twitterscraper.twitter.utils;

import twitter4j.Status;

public class TweetPrinter {

    private final Status tweet;

    public TweetPrinter(Status tweet) {
        this.tweet = tweet;
    }

    @Override
    public String toString() {
        return String.format("@%s - %s (%s)",
                tweet.getUser().getScreenName(),
                getText(),
                getTweetUrl());
    }

    String getText() {
        String text = tweet.getRetweetedStatus() == null ?
                tweet.getText() :
                tweet.getRetweetedStatus().getText();
        return text.replace("\n", "\\n");
    }

    private String getTweetUrl() {
        return "https://twitter.com/" + tweet.getUser().getScreenName() + "/status/" + tweet.getId();
    }
}
