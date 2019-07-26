package com.twitterscraper.server;

import com.google.inject.Inject;
import com.twitterscraper.Component;
import com.twitterscraper.db.DatabaseWrapper;
import io.javalin.Javalin;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.twitterscraper.api.Api.getAsync;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Server extends Component {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private final Javalin app;
  private final DatabaseWrapper db;

  @Inject
  Server(
      final Javalin app,
      final DatabaseWrapper db) {
    this.app = app;
    this.db = db;
  }

  @Override
  public void start() {
    this.app
        .enableMicrometer()
        .enableCorsForAllOrigins()
        .enableCaseSensitiveUrls()
        .enableRouteOverview("help")
        .error(404, ctx ->
            ctx.result(String.format("Could not find anything at \"%s\"" +
                " - What were you hoping to find?", ctx.url())))
        .requestLogger((ctx, time) -> {
          if (time > 10 || !"get".equals(ctx.method().toLowerCase())) {
            log.info("{} on '{}' from {} took {}ms",
                ctx.method(), ctx.path(), ctx.ip(), time);
          }
        })
        .routes(() -> {
          path("collection", () -> {
            getAsync("/:name", ctx ->
                this.db.getAll(ctx.pathParam("name")));
            getAsync("/:name/count", ctx ->
                new Document("count",
                    this.db.count(ctx.pathParam("name"))));
          });
        })
        .start(8080);
  }

  @Override
  public String name() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void run() {
    // No-op, overriding the start function so that this only runs once
  }
}
