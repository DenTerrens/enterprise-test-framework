package com.automation.framework.api.service;

import com.automation.framework.api.client.ApiClient;
import com.automation.framework.config.ConfigManager;
import io.restassured.response.Response;
import java.util.Map;

public class AuthApi {
  private final ApiClient client = new ApiClient(ConfigManager.get("api.baseUrl"), Map.of());

  public Response login(String username, String password) {
    return client.post("/auth/login", Map.of("username", username, "password", password));
  }
}
