package com.intapp.junit.postgres;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.*;
import org.junit.rules.ExternalResource;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Junit external resource to run Embedded postgres container.
 * It helps you write tests with Junit which require Postgres database.
 */
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
            this.container = createContainer(dockerClient, postgresContainer.getPostgresPort(), postgresContainer.getExposedPort());

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


    /**
     * Execute sql query
     * @param sql - SQL query
     * @return success/failure
     */
    public boolean executeSQL(String sql) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        String host = imageConfig.getExposedHost();
        int port = imageConfig.getExposedPort();
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

    private ContainerCreation createContainer(DockerClient docker, int postgresPort, int exposedPort) throws Exception {
        Map<String, List<PortBinding>> portBindings = new HashMap<>();
        portBindings.put(String.valueOf(postgresPort + "/tcp"), Arrays.asList(PortBinding.of("0.0.0.0", exposedPort)));

        HostConfig hostConfig = HostConfig.builder()
                .portBindings(portBindings)
                .build();

        ContainerConfig containerConfig = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .exposedPorts(postgresPort + "/tcp")
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

    public String getExposedHost() {
        return imageConfig.getExposedHost();
    }

    public int getExposedPort() {
        return imageConfig.getExposedPort();
    }

    public static class Builder {

        private static final String DEFAULT_CONTAINER = "postgres";
        private static final String DEFAULT_POSTGRES_USER = "postgres";
        private static final String DEFAULT_POSTGRES_PASSWORD = "";
        private static final String DEFAULT_BASE_NAME = "postgres";
        private static final String DEFAULT_POSTGRES_HOST = "127.0.0.1";
        private static final int DEFAULT_POSTGRES_PORT = 5432;
        private static final int DEFAULT_EXPOSED_PORT = 5432;

        private PostgresContainer postgresContainer;

        private static String getHost() {
            String result = DEFAULT_POSTGRES_HOST;
            try {
                if (System.getenv("DOCKER_HOST") != null) {
                    URI dockerLink = new URI(System.getenv("DOCKER_HOST"));
                    result = dockerLink.getHost() != null ?  dockerLink.getHost(): result;
                }
            }  catch (URISyntaxException ex) {
                //no op
            }
            return result;
        }

        /**
         * Create builder for Postgres container. Initialize default parameters
         */
        public Builder() {
            postgresContainer = new PostgresContainer();
            postgresContainer.setContainerImage(DEFAULT_CONTAINER);
            postgresContainer.setPostgresUser(DEFAULT_POSTGRES_USER);
            postgresContainer.setPostgresPassword(DEFAULT_POSTGRES_PASSWORD);
            postgresContainer.setDatabaseName(DEFAULT_BASE_NAME);
            postgresContainer.setExposedHost(getHost());
            postgresContainer.setPostgresPort(DEFAULT_POSTGRES_PORT);
            postgresContainer.setExposedPort(DEFAULT_EXPOSED_PORT);
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
            this.postgresContainer.setExposedHost(postgresHost);
            return this;
        }

        public Builder setPostgresPort(int postgresPort) {
            this.postgresContainer.setPostgresPort(postgresPort);
            return this;
        }

        public Builder setExposedPort(int exposedPort) {
            this.postgresContainer.setExposedPort(exposedPort);
            return this;
        }

        public EmbeddedPostgresDocker build() {
            return new EmbeddedPostgresDocker(postgresContainer);
        }
    }
}