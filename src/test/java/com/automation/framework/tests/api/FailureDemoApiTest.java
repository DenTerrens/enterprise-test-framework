package com.automation.framework.tests.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.automation.framework.tests.base.BaseApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@DisplayName("API Failure Demo")
@Tag("api")
@Tag("demo-failure")
@EnabledIfSystemProperty(named = "includeFailureDemos", matches = "true")
public class FailureDemoApiTest extends BaseApiTest {
  @Test
  @DisplayName("Show how Allure reports an API status mismatch")
  void failureDemoShowsStatusMismatchInAllure() {
    int actualStatus = usersApi.getUser(1).statusCode();
    assertThat(actualStatus).as("Intentional demo failure for report screenshots").isEqualTo(201);
  }
}
