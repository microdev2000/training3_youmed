package vn.youmed.build;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;
import vn.youmed.constant.MaximumCapacity;
import vn.youmed.constant.ConstantSharedMap;
import vn.youmed.constant.VirtualAddress;

public class SubServerBuilder extends AbstractVerticle {

	@Override
	public void start() {
		vertx.sharedData().<String, String>getClusterWideMap(ConstantSharedMap.ADDRESS_TO_COMMUNICATE, res -> {
			if (res.succeeded()) {
				System.out.println("Join shared map for register maximum request able to handle success!");
				AsyncMap<String, String> dataMap = res.result();
				dataMap.put(VirtualAddress.IP, VirtualAddress.EVENTBUS_ADDRESS, result -> {
					if (result.succeeded()) {
						System.out.println("Register address for communicate is:" + VirtualAddress.EVENTBUS_ADDRESS
								+ " to Gateway success!");
					} else {
						System.out.println("Register address for communicate to Gateway error!");
						System.out.println(result.cause());
					}
				});
			}

			else {
				System.out.println("Join shared map for register maximum request able to handle error!");
				System.out.println(res.cause());
			}

		});

		vertx.sharedData().<String, Integer>getClusterWideMap(ConstantSharedMap.MAX_REQUEST_ABLE_TO_HANDLE, res -> {
			if (res.succeeded()) {
				System.out.println("Join shared map for register maximum request able to handle success!");
				AsyncMap<String, Integer> dataMap = res.result();
				dataMap.put(VirtualAddress.IP, MaximumCapacity.MAXIMUM, result -> {
					if (result.succeeded()) {
						System.out.println("Register maximum request able with value is: " + MaximumCapacity.MAXIMUM
								+ " to Gateway success!");

					} else {
						System.out.println("Register maximum request able to handle to Gateway error!");
						System.out.println(result.cause());
					}
				});
			}

			else {
				System.out.println("Join shared map for register maximum request able to handle error!");
				System.out.println(res.cause());
			}

		});

		MessageConsumer<JsonObject> server3 = vertx.eventBus().consumer(VirtualAddress.EVENTBUS_ADDRESS);
		server3.handler(msg -> {
			System.out.println(msg.body());
			vertx.sharedData().<String, Integer>getClusterWideMap(ConstantSharedMap.MAX_REQUEST_ABLE_TO_HANDLE, res -> {
				AsyncMap<String, Integer> dataMap = res.result();
				dataMap.get(VirtualAddress.IP, han -> {
					if (han.succeeded()) {
						int z = han.result();
						z = z - 1;
						dataMap.put(VirtualAddress.IP, z, result -> {
							if (result.succeeded()) {
								System.out.println("Subtract 1 value successful");
							}
						});

						JsonObject jsonObject = new JsonObject();
						jsonObject.put(VirtualAddress.IP, "Youmed");
						msg.reply(jsonObject);
						dataMap.get(VirtualAddress.IP, total -> {
							if (total.succeeded()) {
								int ou = total.result();
								dataMap.put(VirtualAddress.IP, (ou + 1), m -> {
									if (m.succeeded()) {
										System.out.println("Add 1 value successful");
									} else {
										System.out.println(m.cause());

									}
								});
							} else {
								System.out.println(total.cause());

							}

						});

					} else {
						System.out.println(res.cause());

					}
				});

			});

		});

	}

}
