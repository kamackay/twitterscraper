package com.twitterscraper.monitors;

import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.model.Query;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMonitor {

    volatile List<Query> queries;

    protected final DatabaseWrapper db;

    public AbstractMonitor(final DatabaseWrapper db) {
        this.db = db;
        queries = new ArrayList<>();
    }

    public void setQueries(List<Query> queries) {
        this.queries.clear();
        this.queries.addAll(queries);
    }

    public abstract void run();
}
