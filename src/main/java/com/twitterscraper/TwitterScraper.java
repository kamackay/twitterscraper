package com.twitterscraper;

import com.twitterscraper.logging.Logger;
import com.twitterscraper.twitter.utils.TweetPrinter;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

class TwitterScraper {

    private static final Logger logger = new Logger(TwitterScraper.class);
    private List<Query> queries;
    private final Twitter twitter;
    private Consumer<Status> handleTweet;

    TwitterScraper() {
        twitter = getTwitter();
    }

    private Twitter getTwitter() {
        return new TwitterFactory(new ConfigurationBuilder()
                .setTweetModeExtended(true)
                .build())
                .getInstance();
    }

    /**
     * Run the configured Queries and handle the results
     */
    void run() {
        checkLimits();
        queries.forEach(query -> {
            try {
                QueryResult result;
                result = twitter.search(query);
                logger.log("");
                logger.log("Results for: " + query.toString());
                logger.log("");
                result.getTweets().forEach(this::handleTweet);
            } catch (Exception e) {
                logger.e(e);
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
        logger.log(new TweetPrinter(tweet).toString());
    }

    private void checkLimits() {
        try {
            logger.log("Checking tweets as " + twitter.getScreenName());
            twitter.getRateLimitStatus().forEach((key, value) -> {
                if (value.getRemaining() != value.getLimit()) {
                    logger.log(String.format("%s: %d/%d - %d seconds",
                            key,
                            value.getRemaining(),
                            value.getLimit(),
                            value.getSecondsUntilReset()));
                }
            });
        } catch (TwitterException e) {
            logger.e(e);
        }
        logger.log("");
    }
}
