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
import vn.youmed.handler.HandleAddress;

public class GatewayServer extends AbstractVerticle {

	@Override
	public void start() {
		vertx.sharedData().<String, String>getClusterWideMap(ConstantSharedMap.ADDRESS_TO_COMMUNICATE, res -> {
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
		vertx.sharedData().<String, String>getClusterWideMap(ConstantSharedMap.ADDRESS_TO_COMMUNICATE, res -> {
			if (res.succeeded()) {
				AsyncMap<String, String> dataMap = res.result();
				dataMap.keys(keys -> {
					if (keys.succeeded()) {
						Future<String> future = Future.<String>future();
						future.setHandler(listener -> {
							if (listener.succeeded()) {
								JsonObject json = new JsonObject().put("what is your IP?", "What is your company ?");
								vertx.eventBus().send(listener.result(), json, msg -> {
									if (msg.succeeded()) {
										rc.response().putHeader("Content-Type", "application/json ;charset=utf-8")
												.end(msg.result().body().toString());
									} else {

									}
								});
							} else {
							}
						});
						new HandleAddress(vertx).bestServer(keys, future);
					}
				});

			} else {
				System.out.println("Initialized shared map for register address to communicate failed!");
				System.out.println(res.cause());
			}
		});

	}

}
