package com.intapp.junit.postgres;

class PostgresContainer {

    private String containerImage;
    private String postgresUser;
    private String postgresPassword;
    private String databaseName;
    private String exposedHost;
    private int postgresPort;
    private int exposedPort;

    public String getContainerImage() {
        return containerImage;
    }

    public void setContainerImage(String containerImage) {
        this.containerImage = containerImage;
    }

    public String getPostgresUser() {
        return postgresUser;
    }

    public void setPostgresUser(String postgresUser) {
        this.postgresUser = postgresUser;
    }

    public String getPostgresPassword() {
        return postgresPassword;
    }

    public void setPostgresPassword(String postgresPassword) {
        this.postgresPassword = postgresPassword;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getExposedHost() {
        return exposedHost;
    }

    public void setExposedHost(String exposedHost) {
        this.exposedHost = exposedHost;
    }

    public int getPostgresPort() {
        return postgresPort;
    }

    public void setPostgresPort(int postgresPort) {
        this.postgresPort = postgresPort;
    }

    public int getExposedPort() {
        return exposedPort;
    }

    public void setExposedPort(int exposedPort) {
        this.exposedPort = exposedPort;
    }
}
