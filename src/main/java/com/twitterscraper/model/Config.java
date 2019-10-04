package com.twitterscraper.model;

import com.google.gson.Gson;
import com.twitterscraper.utils.QueryBuilder;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import static com.twitterscraper.utils.Utils.getLogger;
import static java.util.stream.Collectors.toList;

public class Config {
  private static Logger logger = getLogger(Config.class);

  public List<QueryModel> queries;
  public boolean runUpdater;

  /**
   * Get the config from the config.json file
   *
   * @return The Config Object, converted from json
   */
  public static Config get() {
    try {
      return new Gson().fromJson(new FileReader("config.json"), Config.class);
    } catch (FileNotFoundException e) {
      logger.error("Error Reading Config File", e);
      return null;
    }
  }

  public List<Query> convertQueries() {
    return queries.stream()
        .map(this::convertQuery)
        .map(QueryBuilder::build)
        .collect(toList());
  }

  private QueryBuilder convertQuery(QueryModel query) {
    return new QueryBuilder()
        .setModel(query)
        .addMentions(query.mentions)
        .addQuotes(query.quotes)
        .addHashtags(query.hashtags)
        .setIncludeRetweets(query.includeRetweets);
  }


}
