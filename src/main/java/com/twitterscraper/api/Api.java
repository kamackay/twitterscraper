package com.twitterscraper.api;

import io.javalin.apibuilder.ApiBuilder;
import io.javalin.http.Context;
import org.bson.Document;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class Api {
  public static void getAsync(final String path, final Function<Context, ?> resolver) {
    ApiBuilder.get(path, ctx ->
        ctx.json(CompletableFuture.supplyAsync(() -> {
          return resolver.apply(ctx);
        })));
  }

  public static Document makeJson(final Map<String, Object> data) {
    final Document doc = new Document();
    data.forEach(doc::append);
    return doc;
  }
}
