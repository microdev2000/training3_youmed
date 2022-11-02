package vn.youmed.build;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import vn.youmed.constant.ConstantSharedMap;
import vn.youmed.handler.HandlerAddress;

public class GatewayServer extends AbstractVerticle {

	@Override
	public void start() {
		vertx.sharedData().<String, String>getClusterWideMap(ConstantSharedMap.PRESENT_VALUE, res -> {
			if (res.succeeded()) {
				System.out.println(
						"Initialization of shared map with registration of communication address successfully!");
			} else {
				System.out.println("Initialization of shared map of communication address registration failed");
				System.out.println(res.cause());
			}
		});

		vertx.sharedData().<String, Integer>getClusterWideMap(ConstantSharedMap.MAX_REQUEST_ABLE_TO_HANDLE, res -> {
			if (res.succeeded()) {
				System.out.println(
						"Initialize shared map registers the maximum number of request processing successfully!");
			} else {
				System.out.println(
						"Initialize shared map registers the maximum number of request processing successfully failed!");
				System.out.println(res.cause());
			}
		});

		HttpServer server = vertx.createHttpServer();
		Router testRouter = Router.router(vertx);
		testRouter.route("/test").handler(BodyHandler.create());
		testRouter.get("/test").handler(this::chooseServer);
		server.requestHandler(testRouter).listen(4545);

	}

	private void chooseServer(RoutingContext rc) {
		vertx.sharedData().<String, String>getClusterWideMap(ConstantSharedMap.PRESENT_VALUE, res -> {
			if (res.succeeded()) {
				AsyncMap<String, String> dataMap = res.result();
				dataMap.keys(keys -> {
					if (keys.succeeded()) {
						Future<String> future = Future.<String>future();
						future.setHandler(listener -> {
							if (listener.succeeded()) {
								JsonObject json = new JsonObject().put("what is your name?", "What is your company ?");
								vertx.eventBus().send(listener.result(), json, msg -> {
									if (msg.succeeded()) {
									} else {
										Future<String> future2 = Future.future();
										future2.setHandler(s -> {
											if (s.succeeded()) {
												this.reHanlde();
											} else {
												System.out.println(s.cause());
											}
										});
										handleElse(listener.result(), future2);

									}

								});
							} else {
								System.out.println(listener.cause().getMessage());

							}

						});
						new HandlerAddress(vertx).bestServer(keys, future);
					}
				});

			} else {
				System.out.println("Initialized shared map for register address to communicate failed!");
				System.out.println(res.cause());
			}
		});
		rc.response().end("Hello Youmed");

	}

	private void handleElse(String name, Future<String> future) {
		System.out.println(name + " is the best, but " + name + " is not responding "
				+ "\nSo the request will be sent to the remaining servers");
		Future<String> future2 = Future.future();
		future2.setHandler(kk -> {
			if (kk.succeeded()) {
				future.complete();

			}
		});

		new HandlerAddress(vertx).removeMaxRequest(name, future2);
	}

	private void reHanlde() {
		vertx.sharedData().<String, String>getClusterWideMap(ConstantSharedMap.PRESENT_VALUE, res -> {
			if (res.succeeded()) {
				AsyncMap<String, String> dataMap = res.result();
				dataMap.keys(keys -> {
					if (keys.succeeded()) {
						Future<String> future = Future.<String>future();
						future.setHandler(listener -> {
							if (listener.succeeded()) {
								JsonObject json = new JsonObject().put("what is your name?", "What is your company ?");
								vertx.eventBus().send(listener.result(), json, msg -> {
									if (msg.succeeded()) {
									}
								});
							} else {
							}

						});
						new HandlerAddress(vertx).bestServer(keys, future);
					}
				});

			} else {
				System.out.println(res.cause());
			}
		});

	}

}
