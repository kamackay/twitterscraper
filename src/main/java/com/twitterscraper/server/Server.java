package com.twitterscraper.server;

import com.google.inject.Inject;
import com.twitterscraper.Component;
import com.twitterscraper.db.DatabaseWrapperImpl;
import com.twitterscraper.model.Tuple;
import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bson.Document;

import java.util.Objects;

import static com.twitterscraper.api.Api.getAsync;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

@Slf4j
public class Server extends Component {

  private final Javalin app;
  private final DatabaseWrapperImpl db;
  private final OkHttpClient httpClient = new OkHttpClient();

  @Inject
  Server(
      final Javalin app,
      final DatabaseWrapperImpl db) {
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
          if(time > 10 || !"get".equals(ctx.method().toLowerCase())) {
            log.info("{} on '{}' from {} took {}ms",
                ctx.method(), ctx.path(), ctx.ip(), time);
          }
        })
        .routes(() -> {
          path("collection", () -> {
            getAsync("/:name", ctx ->
                this.db.getAll(ctx.pathParam("name")));
            delete("/:name", ctx -> {
              this.db.delete(ctx.pathParam("name"));
            });
            getAsync("/:name/count", ctx ->
                new Document("count",
                    this.db.count(ctx.pathParam("name"))));
          });


          getAsync("/", ctx -> {
            final Document doc = new Document();
            db.getCollections().parallelStream()
                .map(name -> Tuple.of(name, this.db.count(name)))
                .forEach(tuple -> doc.append(tuple.getLeft(), tuple.getRight()));
            return doc;
          });

          get("/favicon.ico", ctx -> {
            final String url = "https://developer.twitter.com/favicon.ico";
            ctx.redirect(url);
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
