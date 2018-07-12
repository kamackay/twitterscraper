package com.twitterscraper.twitter.utils;

import twitter4j.Query;

public class QueryBuilder {

    private final StringBuilder queryString;
    private boolean includeRetweets;
    private final Query query;

    public QueryBuilder() {
        queryString = new StringBuilder();
        includeRetweets = false;
        query = new Query();
    }

    public QueryBuilder(String initialText) {
        this();
        add(initialText);
    }

    public QueryBuilder add(String queryText) {
        queryString.append(queryText);
        return this;
    }

    public QueryBuilder setIncludeRetweets(boolean includeRetweets) {
        this.includeRetweets = includeRetweets;
        return this;
    }

    public QueryBuilder setQueryLimit(final int limit) {
        this.query.setCount(limit);
        return this;
    }

    public Query build() {
        if (!includeRetweets) queryString.append(" +exclude:retweets");
        System.out.println("Query = '" + queryString.toString() + "'");
        query.setQuery(queryString.toString());
        return query;
    }
}
