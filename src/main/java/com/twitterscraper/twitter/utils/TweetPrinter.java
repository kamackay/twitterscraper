package com.twitterscraper.twitter.utils;

import com.google.gson.Gson;
import twitter4j.Place;
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

    public String toJson() {
        return new Gson().toJson(tweet);
    }

    public String getText() {
        String text = tweet.getRetweetedStatus() == null ?
                tweet.getText() :
                tweet.getRetweetedStatus().getText();
        return text.replace("\n", "\\n");
    }

    private String getTweetUrl() {
        return "https://twitter.com/" + tweet.getUser().getScreenName() + "/status/" + tweet.getId();
    }

    private Place getPlace() {
        return tweet.getPlace();
    }
}
