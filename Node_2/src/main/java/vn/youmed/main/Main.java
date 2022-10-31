package vn.youmed.main;

import java.net.InetAddress;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import vn.youmed.build.SubServerBuilder;
import vn.youmed.constant.VirtualAddress;

public class Main {
	public static void main(String[] args) {
		 final VertxOptions vertxOptions = new VertxOptions();
	        EventBusOptions eventBusOptions = new EventBusOptions();
	        String hostAddress = VirtualAddress.IP;
	        vertxOptions.setEventBusOptions(eventBusOptions).getEventBusOptions().setHost(hostAddress);
	        ClusterManager mgr = new HazelcastClusterManager();
			VertxOptions options = new VertxOptions().setClusterManager(mgr);

		Vertx.clusteredVertx(options, res -> {
			if (res.succeeded()) {
				res.result().deployVerticle(SubServerBuilder.class.getName(), res2 -> {
					if (res2.succeeded()) {
						System.out.println("Server with IP: " + VirtualAddress.IP + " start success!");
					}
				});

			} else {
				System.out.println("Server with IP: " + VirtualAddress.IP + " start error!");
				System.out.println(res.cause());
			}
		});

	}
}
