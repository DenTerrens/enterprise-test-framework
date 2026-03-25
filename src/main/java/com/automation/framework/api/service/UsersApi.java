package com.automation.framework.api.service;

import com.automation.framework.api.client.ApiClient;
import com.automation.framework.config.ConfigManager;
import io.restassured.response.Response;
import java.util.Map;

public class UsersApi {
  private final ApiClient client = new ApiClient(ConfigManager.get("api.baseUrl"), Map.of());

  public Response getUser(int userId) {
    return client.get("/users/" + userId);
  }

  public Response createUser(Object payload) {
    return client.post("/users", payload);
  }

  public Response updateUser(int userId, Object payload) {
    return client.put("/users/" + userId, payload);
  }

  public Response patchUser(int userId, Object payload) {
    return client.patch("/users/" + userId, payload);
  }

  public Response deleteUser(int userId) {
    return client.delete("/users/" + userId);
  }

  public Response getUnknownResource() {
    return client.get("/unknown-endpoint");
  }

  public Response listUsers() {
    return client.get("/users");
  }
}
