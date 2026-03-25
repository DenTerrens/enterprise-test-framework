package com.automation.framework.tests.demo;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;

import com.automation.framework.tests.base.BaseDemoApiTest;
import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@DisplayName("Demo API CRUD Verification")
@Tag("api")
@Tag("demo")
@Tag("regression")
@ResourceLock("demoApp")
class DemoApiCrudVerificationTest extends BaseDemoApiTest {
  private int seededUserId() {
    return usersApi.listUsers().jsonPath().getInt("[0].userId");
  }

  @Test
  @DisplayName("Verify authenticated API CRUD operations work end to end")
  void authenticatedCrudFlowWorks() {
    Map<String, String> createPayload = DemoUserDataFactory.newUser("ApiCreate");
    Response created = usersApi.createUser(createPayload);
    int userId = created.jsonPath().getInt("userId");

    assertThat(created.statusCode()).isEqualTo(201);
    assertThat(created.jsonPath().getString("email")).isEqualTo(createPayload.get("email"));

    Map<String, String> updatePayload = DemoUserDataFactory.updatedUser("ApiUpdate");
    Response updated = usersApi.updateUser(userId, updatePayload);
    assertThat(updated.statusCode()).isEqualTo(200);
    assertThat(updated.jsonPath().getString("status")).isEqualTo("DISABLED");

    Response patched = usersApi.patchUser(userId, Map.of("status", "ACTIVE"));
    assertThat(patched.statusCode()).isEqualTo(200);
    assertThat(patched.jsonPath().getString("status")).isEqualTo("ACTIVE");

    Response deleted = usersApi.deleteUser(userId);
    assertThat(deleted.statusCode()).isEqualTo(200);
  }

  @Test
  @DisplayName("Verify user responses match the documented local schema")
  void userResponsesMatchSchema() {
    usersApi
        .getUser(seededUserId())
        .then()
        .statusCode(200)
        .body(matchesJsonSchemaInClasspath("data/api/demo-user-schema.json"));
  }

  @Test
  @DisplayName("Verify unknown local API resources return not found")
  void unknownResourceReturnsNotFound() {
    assertThat(usersApi.getUnknownResource().statusCode()).isEqualTo(404);
  }

  @Test
  @DisplayName("Verify repeated GET requests are idempotent for the same seeded user")
  void repeatedGetsRemainIdempotent() {
    int seededUserId = seededUserId();
    String first = usersApi.getUser(seededUserId).asString();
    String second = usersApi.getUser(seededUserId).asString();
    assertThat(first).isEqualTo(second);
  }
}
