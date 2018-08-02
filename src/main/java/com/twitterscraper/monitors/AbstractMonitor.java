package com.twitterscraper.monitors;

import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.model.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMonitor {

    final DatabaseWrapper db;
    volatile List<Query> queries;

    public AbstractMonitor(
            final DatabaseWrapper db) {
        this.db = db;
        queries = new ArrayList<>();
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean running;

    public void start() {
        running = true;
        while (running) {
            try {
                new Thread(this::run).start();
                Thread.sleep(getFrequency() * 1000);
            } catch (Exception e) {
                logger.error("Error running Monitor", e);
            }
        }
    }

    public void setQueries(List<Query> queries) {
        this.queries.clear();
        this.queries.addAll(queries);
    }

    public void stop() {
        running = false;
    }

    abstract void run();

    /**
     * @return - Frequency in seconds to run monitor
     */
    abstract int getFrequency();
}
