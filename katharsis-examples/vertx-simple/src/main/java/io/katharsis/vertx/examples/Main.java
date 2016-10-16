package io.katharsis.vertx.examples;


import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    public static void main(String[] args) {
        log.info("Hello ");
        // We don't use default methods from Vertx.vertx() to be compatible with Java 7.
        Vertx vertx = Vertx.factory.vertx();

        vertx.deployVerticle(new KatharsisVerticle(vertx));

    }
}
