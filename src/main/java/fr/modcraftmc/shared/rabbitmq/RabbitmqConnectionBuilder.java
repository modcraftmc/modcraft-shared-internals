package fr.modcraftmc.shared.rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Recoverable;
import com.rabbitmq.client.RecoveryListener;
import fr.modcraftmc.shared.ModcraftSharedInternals;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitmqConnectionBuilder {

    private String host;
    private int port;
    private String username;
    private String password;
    private String virtualHost;
    private Runnable onHeartbeatFailed;
    private Runnable onHeartbeatSucceeded;

    public RabbitmqConnectionBuilder() {
    }


    public RabbitmqConnectionBuilder host(String host) {
        this.host = host;
        return this;
    }

    public RabbitmqConnectionBuilder port(int port) {
        this.port = port;
        return this;
    }

    public RabbitmqConnectionBuilder username(String username) {
        this.username = username;
        return this;
    }

    public RabbitmqConnectionBuilder password(String password) {
        this.password = password;
        return this;
    }

    public RabbitmqConnectionBuilder virtualHost(String virtualhost) {
        this.virtualHost = virtualhost;
        return this;
    }

    public RabbitmqConnectionBuilder onHeartbeatFailed(Runnable onHeartbeatFailed) {
        this.onHeartbeatFailed = onHeartbeatFailed;
        return this;
    }

    // Only triggered if heartbeat had failed before
    public RabbitmqConnectionBuilder onHeartbeatSucceeded(Runnable onHeartbeatSucceeded) {
        this.onHeartbeatSucceeded = onHeartbeatSucceeded;
        return this;
    }

    public RabbitmqConnection build() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);
        Connection connection = factory.newConnection();
        ((Recoverable) connection).addRecoveryListener(new RecoveryListener() {
            @Override
            public void handleRecovery(Recoverable recoverable) {
                ModcraftSharedInternals.LOGGER.warn("RabbitMQ server is back online");
                onHeartbeatSucceeded.run();
            }
            @Override
            public void handleRecoveryStarted(Recoverable recoverable) {
                ModcraftSharedInternals.LOGGER.error("RabbitMQ server is unreachable");
                onHeartbeatFailed.run();
            }
        });
        return new RabbitmqConnection(connection);
    }
}
