package io.vertx.webchat;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.apex.Route;
import io.vertx.ext.apex.Router;


public class WebServer extends AbstractVerticle {
	  @Override
	  public void start() {
		  HttpServer server = vertx.createHttpServer();

		  Router router = Router.router(vertx);
		  
		  //request handler for domain:port/chat
		  router.route("/chat").handler(routingCtx -> {
			  
		  });
		  
		  
		  // failure handler for route /chat/*
		  Route route = router.get("/chat/*");
		  route.failureHandler(failureHandler -> {
			  int code = failureHandler.statusCode();
			  
			  HttpServerResponse response = failureHandler.response();
			  response.setStatusCode(code).end("Failed to process: Status Code: "+code);
		  });
	  }
}
