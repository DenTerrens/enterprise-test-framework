package com.automation.framework.api.service;

import com.automation.framework.api.client.ApiClient;
import com.automation.framework.config.ConfigManager;
import io.restassured.response.Response;
import java.util.Map;

public class UploadsApi {
  private final ApiClient client = new ApiClient(ConfigManager.get("api.baseUrl"), Map.of());

  public Response getUpload(long uploadId) {
    return client.get("/uploads/" + uploadId);
  }
}
