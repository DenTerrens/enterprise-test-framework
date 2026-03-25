package com.automation.framework.tests.base;

import com.automation.framework.api.service.AuthApi;
import com.automation.framework.api.service.UploadsApi;
import com.automation.framework.api.service.UsersApi;
import com.automation.framework.demoapp.DemoAppSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

@ResourceLock("demoApp")
@ResourceLock(Resources.SYSTEM_PROPERTIES)
public abstract class BaseDemoApiTest {
  protected UsersApi usersApi;
  protected UploadsApi uploadsApi;

  @BeforeEach
  void setUpDemoApi() {
    DemoAppSupport.reset();
    System.setProperty("api.auth.type", "none");
    String token = new AuthApi().login("admin", "secret123").jsonPath().getString("token");
    System.setProperty("api.auth.type", "bearer");
    System.setProperty("api.auth.token", token);
    usersApi = new UsersApi();
    uploadsApi = new UploadsApi();
  }

  @AfterEach
  void tearDownDemoApiOverrides() {
    DemoAppSupport.clearOverrides();
  }
}
