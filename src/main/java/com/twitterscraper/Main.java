package com.twitterscraper;

import com.google.gson.Gson;
import com.twitterscraper.logging.Logger;
import com.twitterscraper.model.Config;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class Main {

    private static final Logger logger = new Logger(Main.class);

    public static void main(String[] args) {
        try {
            new TwitterScraper()
                    .setQueryList(getConfig().convertQueries())
                    .setTweetHandler(TwitterScraper::printTweet)
                    .run();
        } catch (Exception e){
            logger.e(e);
        }
    }

    private static Config getConfig() throws FileNotFoundException {
        return new Gson().fromJson(new FileReader("config.json"), Config.class);
    }

}
