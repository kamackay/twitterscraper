package com.twitterscraper.model;

import com.twitterscraper.utils.QueryBuilder;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class Config {

    public List<QueryModel> queries;
    public boolean runUpdater;
    public boolean runAnalysis;

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
