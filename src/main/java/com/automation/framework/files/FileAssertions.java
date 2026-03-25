package com.automation.framework.files;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.commons.csv.CSVRecord;

public final class FileAssertions {
  private FileAssertions() {}

  public static void assertFileExists(Path path) {
    assertThat(Files.exists(path)).as("Expected file to exist: %s", path).isTrue();
  }

  public static void assertTextContains(Path path, String expectedContent) {
    assertThat(FileParsers.readText(path)).contains(expectedContent);
  }

  public static void assertDelimitedRowCount(Path path, char delimiter, int expectedRows) {
    List<CSVRecord> records = FileParsers.readDelimited(path, delimiter);
    assertThat(records).hasSize(expectedRows);
  }

  public static void assertJsonValue(Path path, String fieldName, String expectedValue) {
    assertThat(FileParsers.readJson(path).get(fieldName).asText()).isEqualTo(expectedValue);
  }

  public static void assertXmlNodeText(Path path, String tagName, String expectedValue) {
    assertThat(FileParsers.readXml(path).getElementsByTagName(tagName).item(0).getTextContent())
        .isEqualTo(expectedValue);
  }
}
