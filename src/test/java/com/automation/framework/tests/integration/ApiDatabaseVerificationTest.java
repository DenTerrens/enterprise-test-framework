package com.automation.framework.tests.integration;

import com.automation.framework.api.service.UsersApi;
import com.automation.framework.tests.base.BaseDatabaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("API And Database Verification")
@Tag("integration")
@Execution(ExecutionMode.SAME_THREAD)
class ApiDatabaseVerificationTest extends BaseDatabaseTest {
    private final UsersApi usersApi = new UsersApi();

    @Test
    @DisplayName("Verify API data can be persisted and verified in the audit table")
    void apiResponseCanBePersistedAndVerifiedInDatabase() {
        String email = usersApi.getUser(1).jsonPath().getString("email");
        databaseClient.update("delete from api_audit where audit_id = ?", 2001);
        databaseClient.update("insert into api_audit (audit_id, source, payload_value) values (?, ?, ?)", 2001, "jsonplaceholder", email);

        List<Map<String, Object>> rows = databaseClient.query("select payload_value from api_audit where audit_id = ?", 2001);
        assertThat(rows).singleElement().extracting(row -> row.get("payload_value")).isEqualTo("Sincere@april.biz");
    }

    @Test
    @DisplayName("Verify API payload and database audit data stay aligned for the same transaction")
    void apiPayloadAndDatabaseAuditStayAligned() {
        String email = usersApi.getUser(1).jsonPath().getString("email");
        databaseClient.update("delete from api_audit where audit_id = ?", 3001);
        databaseClient.update("insert into api_audit (audit_id, source, payload_value) values (?, ?, ?)", 3001, "jsonplaceholder", email);

        List<Map<String, Object>> rows = databaseClient.query("select source, payload_value from api_audit where audit_id = ?", 3001);
        assertThat(rows).singleElement().satisfies(row -> {
            assertThat(row.get("source")).isEqualTo("jsonplaceholder");
            assertThat(row.get("payload_value")).isEqualTo(email);
        });
    }
}
