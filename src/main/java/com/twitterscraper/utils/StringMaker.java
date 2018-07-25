package com.twitterscraper.utils;

public class StringMaker {

    private final StringBuilder builder;
    private boolean empty;

    public StringMaker(final String starting) {
        builder = new StringBuilder();
        empty = true;
        append(starting);
    }

    public StringMaker() {
        this(null);
    }

    public StringMaker append(final String s) {
        if (s == null) return this;
        builder.append(s);
        empty &= isNullOrEmpty(s);
        return this;
    }

    public boolean isEmpty() {
        return empty;
    }

    public String toString() {
        return builder.toString();
    }

    private static boolean isNullOrEmpty(final String s) {
        return s == null || s.isEmpty();
    }
}
