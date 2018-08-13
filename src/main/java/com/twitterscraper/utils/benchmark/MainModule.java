package com.twitterscraper.utils.benchmark;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public class MainModule extends AbstractModule {
    @Override
    protected void configure() {
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Benchmark.class), new FunctionInterceptor());
    }
}
