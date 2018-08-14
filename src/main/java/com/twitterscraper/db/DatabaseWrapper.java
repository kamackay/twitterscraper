package com.twitterscraper.db;

import com.twitterscraper.utils.Elective;
import org.bson.Document;
import twitter4j.Status;

import java.util.List;

public interface DatabaseWrapper {
    long getMostRetweets(final String collectionName);

    boolean upsert(final Status tweet, final String name);

    void verifyIndex(final String collectionName);

    long getMostRecent(final String collectionName);

    List<Long> getAllIds(final String collectionName);

    Elective<Document> getById(final String collectionName, final long id);

    List<Long> getAllIds(final String name, final boolean b);
}
