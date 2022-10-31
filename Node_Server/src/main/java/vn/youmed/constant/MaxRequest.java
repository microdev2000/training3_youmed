package vn.youmed.constant;

import java.util.HashMap;
import java.util.Map;

public class MaxRequest {
	public static final Map<String, Integer> constantMap;
	static {
		constantMap = new HashMap<>();
		constantMap.put("127.0.0.1", 10);
		constantMap.put("127.0.0.2", 25);
		constantMap.put("127.0.0.3", 50);

	}

}
