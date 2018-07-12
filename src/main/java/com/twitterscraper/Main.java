package com.twitterscraper;

import com.google.gson.Gson;
import com.twitterscraper.logging.Logger;
import com.twitterscraper.model.Config;

import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Entry point class
 */
public class Main {

    private static final Logger logger = new Logger(Main.class);

    /**
     * Main
     *
     * @param args - args
     */
    public static void main(String[] args) {
        try {
            new TwitterScraper()
                    .setQueryList(getConfig().convertQueries())
                    .setTweetHandler(TwitterScraper::printTweet)
                    .run();
        } catch (Exception e) {
            logger.e(e);
        }
    }

    /**
     * Get the config from the config.json file
     *
     * @return The Config Object, converted from json
     * @throws FileNotFoundException if the config.json file cannot be found
     */
    private static Config getConfig() throws FileNotFoundException {
        return new Gson().fromJson(new FileReader("config.json"), Config.class);
    }

}
