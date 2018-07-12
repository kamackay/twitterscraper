package com.twitterscraper;

import twitter4j.Query;

import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        new TwitterScraper()
                .setQueryList(new ArrayList<>(Arrays.asList(
                        new Query("@nasa"),
                        new Query("#maga")
                )))
                .setTweetHandler(tweet -> {
                    // TODO save to database
                })
                .run();
    }

}
