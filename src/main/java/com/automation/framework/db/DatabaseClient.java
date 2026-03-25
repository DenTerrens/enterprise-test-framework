package com.automation.framework.db;

import com.automation.framework.config.ConfigManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseClient implements AutoCloseable {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseClient.class);
  private final Connection connection;

  public DatabaseClient() {
    try {
      Class.forName(ConfigManager.get("db.driver"));
      this.connection =
          DriverManager.getConnection(
              ConfigManager.get("db.url"),
              ConfigManager.get("db.username"),
              ConfigManager.get("db.password"));
      LOGGER.info("Opened database connection to {}", ConfigManager.get("db.url"));
    } catch (SQLException | ClassNotFoundException exception) {
      throw new IllegalStateException("Unable to initialize database client", exception);
    }
  }

  public Connection connection() {
    return connection;
  }

  public int update(String sql, Object... parameters) {
    LOGGER.info("Executing database update: {}", sql);
    try (PreparedStatement statement = preparedStatement(sql, parameters)) {
      return statement.executeUpdate();
    } catch (SQLException exception) {
      throw new IllegalStateException("Failed to execute update: " + sql, exception);
    }
  }

  public List<Map<String, Object>> query(String sql, Object... parameters) {
    LOGGER.info("Executing database query: {}", sql);
    try (PreparedStatement statement = preparedStatement(sql, parameters);
        ResultSet resultSet = statement.executeQuery()) {
      List<Map<String, Object>> rows = new ArrayList<>();
      int columnCount = resultSet.getMetaData().getColumnCount();
      while (resultSet.next()) {
        Map<String, Object> row = new HashMap<>();
        for (int column = 1; column <= columnCount; column++) {
          row.put(resultSet.getMetaData().getColumnLabel(column), resultSet.getObject(column));
        }
        rows.add(row);
      }
      return rows;
    } catch (SQLException exception) {
      throw new IllegalStateException("Failed to execute query: " + sql, exception);
    }
  }

  private PreparedStatement preparedStatement(String sql, Object... parameters)
      throws SQLException {
    PreparedStatement statement = connection.prepareStatement(sql);
    for (int index = 0; index < parameters.length; index++) {
      statement.setObject(index + 1, parameters[index]);
    }
    return statement;
  }

  @Override
  public void close() {
    try {
      LOGGER.info("Closing database connection");
      connection.close();
    } catch (SQLException exception) {
      throw new IllegalStateException("Unable to close database connection", exception);
    }
  }
}
