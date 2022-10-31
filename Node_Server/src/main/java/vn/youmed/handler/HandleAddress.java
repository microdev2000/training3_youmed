package vn.youmed.handler;

import java.util.Set;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;
import vn.youmed.constant.ConstantSharedMap;

public class HandleAddress {
	private Vertx vertx;

	public HandleAddress(Vertx vertx) {
		this.vertx = vertx;
	}

	public void bestServer(AsyncResult<Set<String>> m, Future<String> future) {
		vertx.sharedData().<String, Integer>getClusterWideMap(ConstantSharedMap.MAX_REQUEST_ABLE_TO_HANDLE, res -> {
			if (res.succeeded()) {
				AsyncMap<String, Integer> data = res.result();
				data.entries(handler -> {
					if (handler.succeeded()) {
						new HandlerMax().maxSe(handler.result(), future);
					} else {
						System.out.println(handler.cause());
					}
				});

			} else {
				System.out.println(res.cause());
			}
		});

	}
}
