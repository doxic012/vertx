package io.vertx.webchat;

import io.vertx.core.AbstractVerticle;



/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class HelloWorldVerticle extends AbstractVerticle {

  @Override
  public void start() {
    // Create an HTTP server which simply returns "Hello World!" to each request.
    vertx.createHttpServer().requestHandler(req -> req.response().end("Hello World!")).listen(8080);
  }
}
