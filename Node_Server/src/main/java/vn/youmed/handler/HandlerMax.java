package vn.youmed.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.vertx.core.Future;
import vn.youmed.constant.MaxRequest;

public class HandlerMax {

	public HandlerMax() {
	}

	public void maxSe(Map<String, Integer> map, Future<String> future) {
		List<String> listOfAddress = new ArrayList<>(map.keySet());
		List<Integer> listOfValue = new ArrayList<>(map.values());
		int[] arr = new int[listOfValue.size()];

		for (int i = 0; i < arr.length; i++) {
			int a = listOfValue.get(i);
			int b = 100 / MaxRequest.constantMap.get(listOfAddress.get(i));
			arr[i] = (a * b) - b;
			System.out.println(listOfAddress.get(i) + "----------" + arr[i] + " %");
		}
		int result = cal(arr, listOfAddress);
		String addressServer = listOfAddress.get(result);
		future.complete(addressServer);

	}

	public int cal(int[] arr, List<String> listOfAddress) {
		int max = arr[0];
		int index = 0;
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
				index = i;
			} else if (arr[i] == max) {
				if (MaxRequest.constantMap.get(listOfAddress.get(i)) > MaxRequest.constantMap
						.get(listOfAddress.get(index))) {
					index = i;
				}
			}
		}
		return index;
	}
}
