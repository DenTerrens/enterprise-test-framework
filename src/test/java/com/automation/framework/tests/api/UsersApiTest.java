package com.automation.framework.tests.api;

import com.automation.framework.tests.base.BaseApiTest;
import com.automation.framework.utils.ResourceHelper;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("API Smoke Verification")
@Tag("api")
@Tag("smoke")
class UsersApiTest extends BaseApiTest {
    @Test
    @DisplayName("Verify single user details are returned from the public API")
    void getSingleUserReturnsExpectedPayload() {
        Response response = usersApi.getUser(1);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("email")).isEqualTo("Sincere@april.biz");
    }

    @Test
    @DisplayName("Verify single user response matches the documented schema")
    void getSingleUserMatchesSchema() {
        usersApi.getUser(1)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("data/api/user-schema.json"));
    }

    @Test
    @DisplayName("Verify user creation returns a created response with the submitted name")
    void createUserReturnsCreatedResponse() {
        Response response = usersApi.createUser(ResourceHelper.readClasspathFile("data/api/create-user.json"));

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getString("name")).isEqualTo("morpheus");
    }

    @Test
    @DisplayName("Verify user update returns the updated payload details")
    void updateUserReturnsUpdatedDetails() {
        Response response = usersApi.updateUser(1, ResourceHelper.readClasspathFile("data/api/update-user.json"));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("name")).isEqualTo("neo");
        assertThat(response.jsonPath().getString("email")).isEqualTo("neo@example.test");
    }

    @Test
    @DisplayName("Verify user deletion returns a successful delete status")
    void deleteUserReturnsSuccessfulStatus() {
        Response response = usersApi.deleteUser(1);

        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("Verify repeated reads remain idempotent for the same user")
    void repeatedReadsRemainIdempotent() {
        Response first = usersApi.getUser(1);
        Response second = usersApi.getUser(1);

        assertThat(first.statusCode()).isEqualTo(200);
        assertThat(second.statusCode()).isEqualTo(200);
        assertThat(first.asPrettyString()).isEqualTo(second.asPrettyString());
    }

    @Test
    @DisplayName("Verify unknown API resources return a not found response")
    void unknownEndpointReturnsNotFound() {
        Response response = usersApi.getUnknownResource();

        assertThat(response.statusCode()).isEqualTo(404);
    }
}
