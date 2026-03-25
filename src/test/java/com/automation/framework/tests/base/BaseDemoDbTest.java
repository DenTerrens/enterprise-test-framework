package com.automation.framework.tests.base;

import com.automation.framework.db.DatabaseClient;
import com.automation.framework.demoapp.DemoAppSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.ResourceLock;

@ResourceLock("demoApp")
public abstract class BaseDemoDbTest {
  protected DatabaseClient databaseClient;

  @BeforeEach
  void setUpDemoDatabase() {
    DemoAppSupport.reset();
    System.setProperty("db.url", DemoAppSupport.dbUrl());
    databaseClient = new DatabaseClient();
  }

  @AfterEach
  void tearDownDemoDatabase() {
    if (databaseClient != null) {
      databaseClient.close();
    }
    DemoAppSupport.clearOverrides();
  }
}
