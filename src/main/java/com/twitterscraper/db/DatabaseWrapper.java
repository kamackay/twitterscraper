package com.twitterscraper.db;

import twitter4j.Status;

public interface DatabaseWrapper {
    long getMostRetweets(final String collectionName);

    boolean upsert(final Status tweet, final String name);

    void verifyIndex(final String collectionName);

    long getMostRecent(final String collectionName);
}
