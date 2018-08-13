package com.twitterscraper.monitors;

import com.twitterscraper.model.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMonitor {

    volatile List<Query> queries;

    public AbstractMonitor() {
        queries = new ArrayList<>();
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public void setQueries(List<Query> queries) {
        this.queries.clear();
        this.queries.addAll(queries);
    }

    public abstract void run();
}
