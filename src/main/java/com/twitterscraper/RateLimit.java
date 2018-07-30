package com.twitterscraper;

public enum RateLimit {
    RATE_LIMIT_STATUS("/application/rate_limit_status"),
    SEARCH_TWEETS("/search/tweets"),
    STATUSES_SHOW("/statuses/show/:id");


    private final String name;

    RateLimit(final String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public String toString() {
        return this.name;
    }
}
