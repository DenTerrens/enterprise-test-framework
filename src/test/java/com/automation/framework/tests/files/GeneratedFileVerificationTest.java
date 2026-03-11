package com.automation.framework.tests.files;

import com.automation.framework.files.FileAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

@DisplayName("Generated File Verification")
@Tag("files")
public class GeneratedFileVerificationTest {
    private static final Path ROOT = Path.of("src", "test", "resources", "data", "files");

    @Test
    @DisplayName("Verify generated text, CSV, TSV, JSON, and XML outputs")
    void verifiesStructuredExecutionArtifacts() {
        FileAssertions.assertFileExists(ROOT.resolve("execution-summary.txt"));
        FileAssertions.assertTextContains(ROOT.resolve("execution-summary.txt"), "Smoke suite completed");
        FileAssertions.assertDelimitedRowCount(ROOT.resolve("orders.csv"), ',', 2);
        FileAssertions.assertDelimitedRowCount(ROOT.resolve("orders.tsv"), '\t', 2);
        FileAssertions.assertJsonValue(ROOT.resolve("summary.json"), "suite", "api-smoke");
        FileAssertions.assertXmlNodeText(ROOT.resolve("summary.xml"), "status", "PASSED");
    }

    @Test
    @DisplayName("Verify artifact files exist before deeper content assertions")
    void verifiesArtifactPresence() {
        FileAssertions.assertFileExists(ROOT.resolve("execution-summary.txt"));
        FileAssertions.assertFileExists(ROOT.resolve("orders.csv"));
        FileAssertions.assertFileExists(ROOT.resolve("orders.tsv"));
        FileAssertions.assertFileExists(ROOT.resolve("summary.json"));
        FileAssertions.assertFileExists(ROOT.resolve("summary.xml"));
    }
}
