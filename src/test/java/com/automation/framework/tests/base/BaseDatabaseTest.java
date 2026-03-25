package com.automation.framework.tests.base;

import com.automation.framework.db.DatabaseClient;
import com.automation.framework.utils.SqlScriptRunner;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class BaseDatabaseTest {
  protected DatabaseClient databaseClient;

  @BeforeEach
  void setUpDatabase() {
    System.setProperty(
        "db.url",
        "jdbc:h2:mem:automation-framework-"
            + UUID.randomUUID()
            + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
    databaseClient = new DatabaseClient();
    SqlScriptRunner.runScript(
        databaseClient.connection(), Path.of("src", "test", "resources", "db", "schema.sql"));
    SqlScriptRunner.runScript(
        databaseClient.connection(), Path.of("src", "test", "resources", "db", "seed.sql"));
  }

  @AfterEach
  void tearDownDatabase() {
    databaseClient.close();
    System.clearProperty("db.url");
  }
}
