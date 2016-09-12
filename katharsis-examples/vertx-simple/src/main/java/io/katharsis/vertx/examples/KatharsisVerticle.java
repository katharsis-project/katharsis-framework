package io.katharsis.vertx.examples;

import io.katharsis.vertx.KatharsisHandler;
import io.katharsis.vertx.KatharsisHandlerFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.RouterImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KatharsisVerticle extends AbstractVerticle {

    final Vertx vertx;
    private Handler<RoutingContext> requestHandler;

    @Override
    public void start(final Future<Void> fut) throws Exception {
        // We don't use default methods to be compatible with Java 7.
        // Create a router object.
        final Router router = new RouterImpl(vertx);

        // Bind "/" to our hello message - so we are still compatible.
        router.route("/").handler(helloHandler());

        KatharsisHandler katharsisGlue = KatharsisHandlerFactory.create(Main.class.getPackage().getName(), "/api");

        router.route("/api/*").handler(katharsisGlue);

        // Create the HTTP server and pass the "accept" method to the request handler.
        vertx.createHttpServer()
                // We do this instead of lambda to be compatible with Java 7.
                .requestHandler(new Handler<HttpServerRequest>() {
                    @Override
                    public void handle(HttpServerRequest request) {
                        router.accept(request);
                    }
                })
                // Retrieve the port from the configuration,
                // default to 8080.
                .listen(config().getInteger("http.port", 8080),
                        // We do this instead of lambda to be compatible with Java 7.
                        new Handler<AsyncResult<HttpServer>>() {
                            @Override
                            public void handle(AsyncResult<HttpServer> result) {
                                if (result.succeeded()) {
                                    fut.complete();
                                } else {
                                    fut.fail(result.cause());
                                }
                            }
                        }
                );
    }

    // We do this instead of lambda to be compatible with Java 7.
    private Handler<RoutingContext> helloHandler() {
        return new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext routingContext) {
                HttpServerResponse response = routingContext.response();
                response.putHeader("content-type", "text/html")
                        .end("<h1>Hello from my first Vert.x 3 application</h1>" +
                                "<a href='/api/projects'>/api/projects</a>");
            }
        };
    }
}
