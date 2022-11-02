package vn.youmed.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;
import vn.youmed.constant.ConstantSharedMap;

public class HandlerMax {
	private Vertx vertx;

	private int counter;

	private long timerId;

	public HandlerMax(Vertx vertx) {
		this.vertx = vertx;
	}

	public void removePresentValue(String name, Future<String> future) {
		vertx.sharedData().<String, Integer>getClusterWideMap(ConstantSharedMap.PRESENT_VALUE, res -> {
			AsyncMap<String, Integer> data = res.result();
			data.remove(name, handler -> {
				if (handler.succeeded()) {
					future.complete();
				} else {
					System.out.println(handler.cause());
				}
			});

		});
	}

	public void chooseServer(Map<String, Integer> map, Future<String> future) {
		vertx.sharedData().<String, Integer>getClusterWideMap(ConstantSharedMap.MAX_REQUEST_ABLE_TO_HANDLE, res -> {
			AsyncMap<String, Integer> data = res.result();
			data.entries(handler -> {
				if (handler.succeeded()) {
					Map<String, Integer> maxValue = handler.result();
					List<String> listOfAddress = new ArrayList<>(map.keySet());
					List<Integer> listOfValue = new ArrayList<>(map.values());

					int[] arr = new int[listOfValue.size()];
					int flag = 0;
					for (int i = 0; i < arr.length; i++) {
						int a = listOfValue.get(i);
						int b = 100 / maxValue.get(listOfAddress.get(i));
						int remaining = (a * b) - b;
						if (remaining < 0) {
							flag++;
							arr[i] = -1;
							continue;
						} else {
							arr[i] = remaining;
						}

						System.out.println(listOfAddress.get(i) + "---------- Remaining: " + (a - 1) + " ---------- "
								+ arr[i] + " %");
					}
					if (flag == arr.length) {
						timerId = vertx.setPeriodic(5000, k -> {
							System.out.println("Trying again..........." + (counter + 1));
							Future<String> subFuture = Future.future();
							subFuture.setHandler(sa -> {
								if (subFuture.failed()) {
									future.fail("Server is overloaded");
									vertx.cancelTimer(timerId);
								} else {
									future.complete(sa.result());
									vertx.cancelTimer(timerId);
								}
							});
							tryRequestAgain(maxValue, subFuture);

							System.out.println(counter);
							if (counter == 2) {
								future.fail("Server is overloaded");
								vertx.cancelTimer(timerId);
							}
							counter++;
						});

					} else {
						int result = cal(arr, listOfAddress, map);
						String addressServer = listOfAddress.get(result);
						System.out.println("\n=======> Selected server: " + addressServer + " <=======");
						System.out.println(".........................................");
						future.complete(addressServer);
					}
				}
			});

		});
	}

	public int cal(int[] arr, List<String> listOfAddress, Map<String, Integer> map) {
		int max = arr[0];
		int index = 0;
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
				index = i;
			} else if (arr[i] == max) {
				if (map.get(listOfAddress.get(i)) > map.get(listOfAddress.get(index))) {
					index = i;
				}
			}
		}
		return index;
	}

	public void tryRequestAgain(Map<String, Integer> maxValue, Future<String> subFuture) {
		vertx.sharedData().<String, Integer>getClusterWideMap(ConstantSharedMap.PRESENT_VALUE, res -> {
			if (res.succeeded()) {
				AsyncMap<String, Integer> data = res.result();
				data.entries(handler -> {
					if (handler.succeeded()) {
						Map<String, Integer> map = handler.result();
						List<String> listOfAddress = new ArrayList<>(map.keySet());
						List<Integer> listOfValue = new ArrayList<>(map.values());

						int[] arr = new int[listOfValue.size()];
						int flag = 0;
						for (int i = 0; i < arr.length; i++) {
							int a = listOfValue.get(i);
							int b = 100 / maxValue.get(listOfAddress.get(i));
							int remaining = (a * b) - b;
							if (remaining < 0) {
								arr[i] = -1;
								flag++;
								continue;
							} else {
								arr[i] = remaining;
							}

							if (flag == arr.length) {
								System.out.println("Sever still overloaded");
								subFuture.fail("Sever still overloaded");

							} else {
								int result = cal(arr, listOfAddress, map);
								String addressServer = listOfAddress.get(result);
								System.out.println("\n=======> Selected server: " + addressServer + " <=======");
								System.out.println(".........................................");
								subFuture.complete(addressServer);
							}
						}
					} else {
						System.out.println(handler.cause());
					}
				});
			}
		});
	}

}
