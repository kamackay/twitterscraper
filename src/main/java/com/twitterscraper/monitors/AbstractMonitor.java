package com.twitterscraper.monitors;

import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.model.Query;
import com.twitterscraper.utils.benchmark.Benchmark;

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

    public void run() {
        new ArrayList<>(queries).parallelStream()
                .map(Query::getName)
                .forEach(this::handleQuery);
    }

    @Benchmark(paramName = true, limit = 1000)
    protected abstract void handleQuery(final String name);
}
