package com.automation.framework.files;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.w3c.dom.Document;

public final class FileParsers {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private FileParsers() {}

  public static String readText(Path path) {
    try {
      return Files.readString(path, StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new UncheckedIOException("Unable to read text file: " + path, exception);
    }
  }

  public static List<CSVRecord> readDelimited(Path path, char delimiter) {
    try {
      Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
      return CSVFormat.DEFAULT
          .builder()
          .setDelimiter(delimiter)
          .setHeader()
          .setSkipHeaderRecord(true)
          .build()
          .parse(reader)
          .getRecords();
    } catch (IOException exception) {
      throw new UncheckedIOException("Unable to read delimited file: " + path, exception);
    }
  }

  public static JsonNode readJson(Path path) {
    try {
      return OBJECT_MAPPER.readTree(path.toFile());
    } catch (IOException exception) {
      throw new UncheckedIOException("Unable to read JSON file: " + path, exception);
    }
  }

  public static Document readXml(Path path) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      return factory.newDocumentBuilder().parse(path.toFile());
    } catch (Exception exception) {
      throw new IllegalStateException("Unable to read XML file: " + path, exception);
    }
  }
}
