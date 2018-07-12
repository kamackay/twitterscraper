package com.twitterscraper;

import com.sun.istack.internal.NotNull;
import twitter4j.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

class TwitterScraper {

    private List<Query> queries;
    private final Twitter twitter;
    private Consumer<Status> handleTweet;

    TwitterScraper() {
        twitter = getTwitter();
    }

    private Twitter getTwitter() {
        return new TwitterFactory().getInstance();
    }

    TwitterScraper run() {
        queries.forEach(query -> {
            try {
                QueryResult result;
                result = twitter.search(query);
                result.getTweets().forEach(this::handleTweet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return this;
    }

    TwitterScraper setTweetHandler(Consumer<Status> handleTweet) {
        this.handleTweet = handleTweet;
        return this;
    }

    TwitterScraper setQueryList(final List<Query> queries) {
        this.queries = queries;
        return this;
    }

    private void handleTweet(Status tweet) {
        Optional.ofNullable(handleTweet).orElse(this::printTweet).accept(tweet);
    }

    private void printTweet(Status tweet) {
        System.out.println(String.format("@%s - %s (%s)",
                tweet.getUser().getScreenName(),
                tweet.getText(),
                getTweetUrl(tweet)));
    }

    private String getTweetUrl(@NotNull Status tweet) {
        return "https://twitter.com/" + tweet.getUser().getScreenName() + "/status/" + tweet.getId();
    }
}
