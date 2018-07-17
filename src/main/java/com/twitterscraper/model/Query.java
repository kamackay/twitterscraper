package com.twitterscraper.model;

public class Query {
    private twitter4j.Query query;
    private QueryModel model;

    public Query(twitter4j.Query query, QueryModel model) {
        this.query = query;
        this.model = model;
    }

    public twitter4j.Query getQuery() {
        return query;
    }

    public Query setQuery(twitter4j.Query query) {
        this.query = query;
        return this;
    }

    public QueryModel getModel() {
        return model;
    }

    public Query setModel(QueryModel model) {
        this.model = model;
        return this;
    }
}