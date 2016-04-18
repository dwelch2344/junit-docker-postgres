package com.intapp.junit.postgres.stuff;

import java.sql.*;

public class TestDao {

    private final String jdbcUrl;
    private final String user;
    private final String password;

    private Connection connection;

    public TestDao(String host, int port, String database, String user, String password) {
        this.jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
        this.user = user;
        this.password = password;
    }

    private Connection getConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            if (connection == null) {
                connection = DriverManager.getConnection(jdbcUrl, user, password);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    public boolean saveTestDto(TestDto dto) {
        String sql = String.format("INSERT INTO test (id, key) VALUES (%d, '%s'); ", dto.getId(), dto.getKey());

        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            boolean result = statement.execute(sql);
            statement.close();
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public TestDto getTestDto(int dtoId) {
        String sql = String.format("SELECT * FROM test WHERE id = %d;", dtoId);

        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            resultSet.next();

            int id = resultSet.getInt("id");
            String key = resultSet.getString("key");

            statement.close();
            return new TestDto(id, key);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
