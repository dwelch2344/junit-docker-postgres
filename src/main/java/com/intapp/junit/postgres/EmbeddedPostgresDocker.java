package com.intapp.junit.postgres;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.*;
import org.junit.rules.ExternalResource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EmbeddedPostgresDocker extends ExternalResource {

    private final DockerClient dockerClient;
    private final PostgresContainer imageConfig;
    private final ContainerCreation container;

    private EmbeddedPostgresDocker(PostgresContainer postgresContainer) {
        try {
            this.imageConfig = postgresContainer;

            AuthConfig authConfig = AuthConfig.fromDockerConfig().build();

            this.dockerClient = DefaultDockerClient.fromEnv()
                    .authConfig(authConfig)
                    .build();

            dockerClient.pull(imageConfig.getContainerImage());
            this.container = createContainer(dockerClient, String.valueOf(postgresContainer.getPostgresPort()));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void before() throws Throwable {
        dockerClient.startContainer(container.id());
        waitPostgresInitialize();
    }

    @Override
    protected void after() {
        try {
            dockerClient.killContainer(container.id());
            dockerClient.removeContainer(container.id());
            dockerClient.close();
        } catch (DockerException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean executeSQL(String sql) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        String host = imageConfig.getPostgresHost();
        int port = imageConfig.getPostgresPort();
        String database = imageConfig.getDatabaseName();

        String user = imageConfig.getPostgresUser();
        String password = imageConfig.getPostgresPassword();

        String jdbsUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
        try (Connection connection = DriverManager.getConnection(jdbsUrl, user, password);
             Statement statement = connection.createStatement()) {
            return statement.execute(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ContainerCreation createContainer(DockerClient docker, String... exposedPorts) throws Exception {
        Map<String, List<PortBinding>> portBindings = new HashMap<>();

        for (String exposedPort : exposedPorts) {
            portBindings.put(exposedPort, Arrays.asList(PortBinding.of("0.0.0.0", Integer.parseInt(exposedPort))));
        }

        HostConfig hostConfig = HostConfig.builder()
                .portBindings(portBindings)
                .build();

        ContainerConfig containerConfig = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .exposedPorts(exposedPorts)
                .image(imageConfig.getContainerImage())
                .networkDisabled(false)
                .build();

        return docker.createContainer(containerConfig);
    }

    private void waitPostgresInitialize() {

        boolean connected = false;
        // Wait only 20 seconds, after that kill container and throw exception
        for (int i = 0; i < 20 && !connected; i++) {
            try {
                TimeUnit.SECONDS.sleep(1);
                connected = this.executeSQL("SELECT 1");
            } catch (Exception ignored) {
            }
        }
        if (!connected) {
            this.after();
            throw new IllegalStateException();
        }
    }

    public String getPostgresUser() {
        return imageConfig.getPostgresUser();
    }

    public String getPostgresPassword() {
        return imageConfig.getPostgresPassword();
    }

    public String getDatabaseName() {
        return imageConfig.getDatabaseName();
    }

    public String getPostgresHost() {
        return imageConfig.getPostgresHost();
    }

    public int getPostgresPort() {
        return imageConfig.getPostgresPort();
    }

    public static class Builder {

        private static final String DEFAULT_CONTAINER = "postgres";
        private static final String DEFAULT_POSTGRES_USER = "postgres";
        private static final String DEFAULT_POSTGRES_PASSWORD = "";
        private static final String DEFAULT_BASE_NAME = "postgres";
        private static final String DEFAULT_POSTGRES_HOST = "127.0.0.1";
        private static final int DEFAULT_POSTGRES_PORT = 5432;

        private PostgresContainer postgresContainer;

        public Builder() {
            postgresContainer = new PostgresContainer();
            postgresContainer.setContainerImage(DEFAULT_CONTAINER);
            postgresContainer.setPostgresUser(DEFAULT_POSTGRES_USER);
            postgresContainer.setPostgresPassword(DEFAULT_POSTGRES_PASSWORD);
            postgresContainer.setDatabaseName(DEFAULT_BASE_NAME);
            postgresContainer.setPostgresHost(DEFAULT_POSTGRES_HOST);
            postgresContainer.setPostgresPort(DEFAULT_POSTGRES_PORT);
        }

        public Builder setContainerImage(String containerImage) {
            this.postgresContainer.setContainerImage(containerImage);
            return this;
        }

        public Builder setPostgresUser(String postgresUser) {
            this.postgresContainer.setPostgresUser(postgresUser);
            return this;
        }

        public Builder setPostgresPassword(String postgresPassword) {
            this.postgresContainer.setPostgresPassword(postgresPassword);
            return this;
        }

        public Builder setDatabaseName(String databaseName) {
            this.postgresContainer.setDatabaseName(databaseName);
            return this;
        }

        public Builder setPostgresHost(String postgresHost) {
            this.postgresContainer.setPostgresHost(postgresHost);
            return this;
        }

        public Builder setPostgresPort(int postgresPort) {
            this.postgresContainer.setPostgresPort(postgresPort);
            return this;
        }

        public EmbeddedPostgresDocker build() {
            return new EmbeddedPostgresDocker(postgresContainer);
        }
    }
}