package com.twitterscraper.server;


import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.twitterscraper.db.DatabaseWrapper;
import com.twitterscraper.db.DatabaseWrapperImpl;
import com.twitterscraper.utils.benchmark.Benchmark;
import com.twitterscraper.utils.benchmark.FunctionInterceptor;

public class ServerModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(DatabaseWrapper.class).to(DatabaseWrapperImpl.class).asEagerSingleton();
    bindInterceptor(Matchers.any(), Matchers.annotatedWith(Benchmark.class), new FunctionInterceptor());
  }
}
