# Junit Postgres docker resource
Junit external resource to run Embedded postgres container. It helps you write tests with Junit which require Postgres database.

[![Build Status](https://travis-ci.org/intappx/junit-docker-postgres.svg?branch=master)](https://travis-ci.org/intappx/junit-docker-postgres)
[![codecov.io](https://codecov.io/github/intappx/junit-docker-postgres/coverage.svg?branch=master)](https://codecov.io/github/intappx/junit-docker-postgres?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/com.intapp/junit-docker-postgres.svg?maxAge=2592000)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.intapp%22%20AND%20a%3A%22junit-docker-postgres%22)

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