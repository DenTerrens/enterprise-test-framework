package com.automation.framework.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class SqlScriptRunner {
  private SqlScriptRunner() {}

  public static void runScript(Connection connection, Path scriptPath) {
    try {
      String script = Files.readString(scriptPath, StandardCharsets.UTF_8);
      for (String statement : script.split(";")) {
        String trimmed = statement.trim();
        if (trimmed.isBlank()) {
          continue;
        }
        try (Statement sqlStatement = connection.createStatement()) {
          sqlStatement.execute(trimmed);
        }
      }
    } catch (IOException exception) {
      throw new UncheckedIOException("Unable to read SQL script: " + scriptPath, exception);
    } catch (SQLException exception) {
      throw new IllegalStateException("Unable to execute SQL script: " + scriptPath, exception);
    }
  }
}
