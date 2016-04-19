# Junit Postgres docker resource
Junit external resource to run Embedded postgres container. It helps you write tests with Junit which require Postgres database.

## How to start:

### Requirements: 
* Docker agent installed on your environment
* Your user account should be inside docker group (access without sudo)
* Also check that your account have access to `.docker/config.json`

### How to use: 
Just add Junit rule for your tests:
```
    @ClassRule
    public static EmbeddedPostgresDocker postgres = new EmbeddedPostgresDocker.Builder()
            .setContainerImage("postgres:9.5")
            .setDatabaseName("postgres")
            .setPostgresUser("postgres")
            .setPostgresPassword("")
            .build();
```

It will run docker container with postgres on your local machine. 
You also have ability to execute sql query 
```
postgres.executeSQL("CREATE TABLE test (id INT PRIMARY KEY, key VARCHAR(256));");
```