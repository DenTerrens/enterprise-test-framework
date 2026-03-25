package com.automation.framework.tests.base;

import com.automation.framework.api.service.UsersApi;

public abstract class BaseApiTest {
  protected final UsersApi usersApi = new UsersApi();
}
