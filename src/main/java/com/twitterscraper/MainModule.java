package com.twitterscraper;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.twitterscraper.analytics.SentimentAnalyzer;
import com.twitterscraper.analytics.SentimentAnalyzerImpl;
import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.utils.benchmark.Benchmark;
import com.twitterscraper.utils.benchmark.FunctionInterceptor;

public class MainModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(DatabaseWrapper.class).asEagerSingleton();
        bind(SentimentAnalyzer.class).to(SentimentAnalyzerImpl.class).asEagerSingleton();
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Benchmark.class), new FunctionInterceptor());
    }
}
