package com.twitterscraper.model;

import com.twitterscraper.twitter.utils.QueryBuilder;
import twitter4j.Query;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class Config {

    public List<QueryModel> queries;

    public List<Query> convertQueries() {
        return queries.stream()
                .map(this::convertQuery)
                .map(QueryBuilder::build)
                .collect(toList());
    }

    private QueryBuilder convertQuery(QueryModel query) {
        return new QueryBuilder()
                .addMentions(query.mentions)
                .addQuotes(query.quotes)
                .addHashtags(query.hashtags)
                .setIncludeRetweets(query.includeRetweets);
    }

}
