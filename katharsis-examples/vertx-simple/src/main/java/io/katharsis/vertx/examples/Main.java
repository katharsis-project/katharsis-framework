package io.katharsis.vertx.examples;

import io.vertx.core.Vertx;

public class Main {

  public static void main(String[] args) {
    // We don't use default methods from Vertx.vertx() to be compatible with Java 7.
    Vertx vertx = Vertx.factory.vertx();

    vertx.deployVerticle(new KatharsisVerticle(vertx));

  }
}
