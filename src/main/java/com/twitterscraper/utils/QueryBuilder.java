package com.twitterscraper.utils;

import org.jetbrains.annotations.NotNull;
import com.twitterscraper.model.QueryModel;
import twitter4j.Query;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;

/**
 * Builder style class to generate a Twitter Query
 */
public class QueryBuilder {

    private final StringBuilder queryString;
    private boolean includeRetweets;
    private final Query query;
    private QueryModel model;

    public QueryBuilder() {
        queryString = new StringBuilder();
        includeRetweets = false;
        query = new Query();
        setQueryLimit(100);
    }

    public QueryBuilder(String initialText) {
        this();
        add(initialText);
    }

    private QueryBuilder add(@NotNull String queryText) {
        queryString.append(" ").append(queryText);
        return this;
    }

    private QueryBuilder addMention(@NotNull String name) {
        return add("@" + name);
    }

    public QueryBuilder addMentions(List<String> list) {
        if (list == null) return this;
        list.forEach(this::addMention);
        return this;
    }

    private QueryBuilder addHashtag(@NotNull String hashtag) {
        return add("#" + hashtag);
    }

    public QueryBuilder addHashtags(List<String> list) {
        if (list == null) return this;
        list.forEach(this::addHashtag);
        return this;
    }

    private QueryBuilder addQuote(@NotNull String quote) {
        return add("\"" + quote + "\"");
    }

    public QueryBuilder addQuotes(List<String> list) {
        if (list == null) return this;
        list.forEach(this::addQuote);
        return this;
    }

    public QueryBuilder setIncludeRetweets(boolean includeRetweets) {
        this.includeRetweets = includeRetweets;
        return this;
    }

    private QueryBuilder setQueryLimit(final int limit) {
        checkState(limit <= 100, "Limit can only be up to 100");
        query.setCount(limit);
        return this;
    }

    public QueryBuilder since(@NotNull String since) {
        query.since(since);
        return this;
    }

    public QueryBuilder since(long id) {
        query.sinceId(id);
        return this;
    }

    public QueryBuilder until(@NotNull String until) {
        query.until(until);
        return this;
    }

    public QueryBuilder until(long id) {
        query.sinceId(id);
        return this;
    }

    public QueryBuilder setModel(QueryModel query) {
        this.model = query;
        return this;
    }

    public com.twitterscraper.model.Query build() {
        if (!includeRetweets) add("+exclude:retweets");
        query.setQuery(queryString.toString());
        return new com.twitterscraper.model.Query(query, model);
    }
}
