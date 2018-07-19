package com.twitterscraper.model;

import java.util.List;

public class QueryModel {
    public List<String> mentions;
    public List<String> quotes;
    public List<String> hashtags;
    public boolean includeRetweets;
    public String queryName;

    public String getQueryName() {
        return queryName;
    }
}
