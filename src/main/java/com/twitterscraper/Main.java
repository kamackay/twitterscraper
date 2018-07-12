package com.twitterscraper;

import com.twitterscraper.twitter.utils.QueryBuilder;

import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        new TwitterScraper()
                .setQueryList(new ArrayList<>(Arrays.asList(
                        new QueryBuilder()
                                .add("#maga")
                                .setQueryLimit(10)
                                .setIncludeRetweets(true)
                                .build()
                )))
                .setTweetHandler(TwitterScraper::printTweet)
                .run();
    }

}
