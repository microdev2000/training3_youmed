package vn.youmed.main;
import java.net.InetAddress;
import java.net.UnknownHostException;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import vn.youmed.build.GatewayServer;

public class App {
	public static void main(String[] args) throws UnknownHostException {
		final VertxOptions vertxOptions = new VertxOptions();
		EventBusOptions eventBusOptions = new EventBusOptions();
		String hostAddress = InetAddress.getLocalHost().getHostAddress();
		vertxOptions.setEventBusOptions(eventBusOptions).getEventBusOptions().setHost(hostAddress);

		HazelcastClusterManager clusterManager = new HazelcastClusterManager();

		vertxOptions.setClusterManager(clusterManager);
		
		Vertx.clusteredVertx(vertxOptions, res -> {
			Vertx result = res.result();
			result.deployVerticle(new GatewayServer(), r -> {
				if (r.succeeded()) {
					System.out.println("Build Gateway Server success!");				

				} else {
					r.cause().printStackTrace();
					System.err.println("Build Gateway Server error!");	
				}
			});
		});
	}
}
