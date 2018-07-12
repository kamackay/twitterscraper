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

    void run() {
        checkLimits();
        queries.forEach(query -> {
            try {
                QueryResult result;
                result = twitter.search(query);
                result.getTweets().forEach(this::handleTweet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // return this;
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
        Optional.ofNullable(handleTweet).orElse(TwitterScraper::printTweet).accept(tweet);
    }

    static void printTweet(Status tweet) {
        System.out.println(String.format("@%s - %s (%s)",
                tweet.getUser().getScreenName(),
                tweet.getText(),
                getTweetUrl(tweet)));
    }

    private static String getTweetUrl(@NotNull Status tweet) {
        return "https://twitter.com/" + tweet.getUser().getScreenName() + "/status/" + tweet.getId();
    }

    private void checkLimits() {
        try {
            twitter.getRateLimitStatus().forEach((key, value) -> {
                if (value.getRemaining() != value.getLimit()) {
                    System.out.println(String.format("%s: %d/%d - %d seconds",
                            key,
                            value.getRemaining(),
                            value.getLimit(),
                            value.getSecondsUntilReset()));
                }
            });
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }
}
