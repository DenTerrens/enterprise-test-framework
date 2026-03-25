package com.automation.framework.tests.db;

import static org.assertj.core.api.Assertions.assertThat;

import com.automation.framework.tests.base.BaseDatabaseTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@DisplayName("Database Verification")
@Tag("db")
@Tag("regression")
@Execution(ExecutionMode.SAME_THREAD)
class DatabaseVerificationTest extends BaseDatabaseTest {
  @Test
  @DisplayName("Verify active customer records are available in the seeded database")
  void canQuerySeededCustomerData() {
    List<Map<String, Object>> rows =
        databaseClient.query(
            "select customer_id, email, status from customer_account where status = ?", "ACTIVE");

    assertThat(rows).hasSize(2);
    assertThat(rows)
        .extracting(row -> row.get("email"))
        .contains("standard_user@demo.local", "api_auditor@demo.local");
  }

  @Test
  @DisplayName("Verify seeded customer emails remain unique across the dataset")
  void seededCustomerEmailsRemainUnique() {
    List<Map<String, Object>> duplicateRows =
        databaseClient.query(
            """
                select email, count(*) as duplicate_count
                from customer_account
                group by email
                having count(*) > 1
                """);

    assertThat(duplicateRows).isEmpty();
  }
}
