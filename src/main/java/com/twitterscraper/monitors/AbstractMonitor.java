package com.twitterscraper.monitors;

import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.model.Query;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.twitterscraper.utils.Utils.getLogger;

public abstract class AbstractMonitor {

  protected final DatabaseWrapper db;
  volatile List<Query> queries;

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
