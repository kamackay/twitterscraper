package com.twitterscraper.twitter.utils;

import com.sun.istack.internal.NotNull;
import twitter4j.Query;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;

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

    private QueryBuilder add(@NotNull String queryText) {
        queryString.append(" ").append(queryText);
        return this;
    }

    public QueryBuilder addMention(@NotNull String name) {
        return add("@" + name);
    }

    public QueryBuilder addMentions(List<String> list) {
        if (list == null) return this;
        list.forEach(this::addMention);
        return this;
    }

    public QueryBuilder addHashtag(@NotNull String hashtag) {
        return add("#" + hashtag);
    }

    public QueryBuilder addHashtags(List<String> list) {
        if (list == null) return this;
        list.forEach(this::addHashtag);
        return this;
    }

    public QueryBuilder addQuote(@NotNull String quote) {
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

    public QueryBuilder setQueryLimit(final int limit) {
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

    public Query build() {
        if (!includeRetweets) add("+exclude:retweets");
        query.setQuery(queryString.toString());
        return query;
    }
}
