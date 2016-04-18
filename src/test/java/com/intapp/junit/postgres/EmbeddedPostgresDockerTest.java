package com.intapp.junit.postgres;

import com.intapp.junit.postgres.stuff.TestDao;
import com.intapp.junit.postgres.stuff.TestDto;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

public class EmbeddedPostgresDockerTest {

    @ClassRule
    public static EmbeddedPostgresDocker postgres = new EmbeddedPostgresDocker.Builder()
            .setContainerImage("postgres:9.5")
            .setDatabaseName("postgres")
            .setPostgresPassword("")
            .setExposedPort(9547)
            .build();

    @Test
    public void testRule_InsertSelect_successful() throws Exception {
        //Arrange
        postgres.executeSQL("CREATE TABLE test (id INT PRIMARY KEY, key VARCHAR(256));");

        TestDao testDao = new TestDao(
                postgres.getExposedHost(),
                postgres.getExposedPort(),
                postgres.getDatabaseName(),
                postgres.getPostgresUser(),
                postgres.getPostgresPassword());

        TestDto testDto = new TestDto(1, "test");

        //Act
        testDao.saveTestDto(testDto);
        TestDto selected = testDao.getTestDto(1);

        //Assert
        Assert.assertEquals(testDto, selected);
    }
}