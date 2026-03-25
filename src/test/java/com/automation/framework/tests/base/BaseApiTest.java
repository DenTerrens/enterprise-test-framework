package com.automation.framework.tests.base;

import com.automation.framework.api.service.UsersApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

@ResourceLock(Resources.SYSTEM_PROPERTIES)
public abstract class BaseApiTest {
  protected UsersApi usersApi;

  @BeforeEach
  void setUpApiClient() {
    usersApi = new UsersApi();
  }
}
