package fr.modcraftmc.shared.mongodb;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MongoDbConnectionBuilder {

    private String host;
    private int port;
    private String username;
    private String password;
    private String authsource;
    private Runnable onHeartbeatFailed;
    private Runnable onHeartbeatSucceeded;

    public MongoDbConnectionBuilder() {
    }


    public MongoDbConnectionBuilder host(String host) {
        this.host = host;
        return this;
    }

    public MongoDbConnectionBuilder port(int port) {
        this.port = port;
        return this;
    }

    public MongoDbConnectionBuilder username(String username) {
        this.username = username;
        return this;
    }

    public MongoDbConnectionBuilder password(String password) {
        this.password = password;
        return this;
    }

    public MongoDbConnectionBuilder authsource(String authsource) {
        this.authsource = authsource;
        return this;
    }

    public MongoDbConnectionBuilder onHeartbeatFailed(Runnable onHeartbeatFailed) {
        this.onHeartbeatFailed = onHeartbeatFailed;
        return this;
    }

    // Only triggered if heartbeat had failed before
    public MongoDbConnectionBuilder onHeartbeatSucceeded(Runnable onHeartbeatSucceeded) {
        this.onHeartbeatSucceeded = onHeartbeatSucceeded;
        return this;
    }

    public MongoClient build() {
        MongoCredential credential = MongoCredential.createCredential(username, authsource, password.toCharArray());
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(List.of(new ServerAddress(host, port))))
                .applyToSocketSettings(builder -> builder.connectTimeout(0, TimeUnit.SECONDS))
                .applyToServerSettings(builder -> builder.addServerMonitorListener(new MongodbServerMonitorListener(onHeartbeatFailed, onHeartbeatSucceeded)))
                .credential(credential)
                .build();

        return MongoClients.create(settings);
    }

}
