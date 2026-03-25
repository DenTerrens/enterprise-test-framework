package com.automation.framework.demoapp;

import com.automation.framework.utils.SqlScriptRunner;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemoAppServer {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String TOKEN = "demo-token";
  private final String dbUrl;
  private int port;
  private HttpServer server;

  public DemoAppServer(String dbUrl) {
    this.dbUrl = dbUrl;
  }

  public void start() {
    if (server != null) {
      return;
    }
    try {
      Class.forName("org.h2.Driver");
      resetData();
      server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      server.createContext("/login", exchange -> writeHtml(exchange, loginPage()));
      server.createContext("/users", exchange -> writeHtml(exchange, usersPage()));
      server.createContext("/api/auth/login", this::handleLogin);
      server.createContext("/api/users", this::handleUsersCollection);
      server.createContext("/api/users/", this::handleUsersById);
      server.createContext("/api/uploads/process", this::handleUploadProcessing);
      server.createContext("/api/uploads/", this::handleUploadLookup);
      server.setExecutor(null);
      server.start();
      port = server.getAddress().getPort();
    } catch (Exception exception) {
      throw new IllegalStateException("Unable to start demo app server", exception);
    }
  }

  public void stop() {
    if (server != null) {
      server.stop(0);
      server = null;
    }
  }

  public void resetData() {
    try (Connection connection = DriverManager.getConnection(dbUrl, "sa", "")) {
      SqlScriptRunner.runScript(
          connection, Path.of("src", "test", "resources", "db", "demo-schema.sql"));
      SqlScriptRunner.runScript(
          connection, Path.of("src", "test", "resources", "db", "demo-seed.sql"));
    } catch (Exception exception) {
      throw new IllegalStateException("Unable to reset demo data", exception);
    }
  }

  public String apiBaseUrl() {
    return "http://127.0.0.1:" + port + "/api";
  }

  public String uiBaseUrl() {
    return "http://127.0.0.1:" + port;
  }

  public String dbUrl() {
    return dbUrl;
  }

  private void handleLogin(HttpExchange exchange) throws IOException {
    if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
      writeJson(exchange, 405, Map.of("message", "Method not allowed"));
      return;
    }
    Map<String, String> payload = readJson(exchange, new TypeReference<>() {});
    boolean valid =
        "admin".equals(payload.get("username")) && "secret123".equals(payload.get("password"));
    if (valid) {
      writeJson(exchange, 200, Map.of("token", TOKEN));
    } else {
      writeJson(exchange, 401, Map.of("message", "Invalid credentials"));
    }
  }

  private void handleUsersCollection(HttpExchange exchange) throws IOException {
    if (!isAuthorized(exchange)) {
      writeJson(exchange, 401, Map.of("message", "Unauthorized"));
      return;
    }
    try (Connection connection = openConnection()) {
      if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
        writeJson(exchange, 200, listUsers(connection));
        return;
      }
      if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
        Map<String, String> payload = readJson(exchange, new TypeReference<>() {});
        long userId = insertUser(connection, payload);
        writeJson(exchange, 201, getUser(connection, userId));
        return;
      }
      writeJson(exchange, 405, Map.of("message", "Method not allowed"));
    } catch (Exception exception) {
      writeJson(exchange, 500, Map.of("message", exception.getMessage()));
    }
  }

  private void handleUsersById(HttpExchange exchange) throws IOException {
    if (!isAuthorized(exchange)) {
      writeJson(exchange, 401, Map.of("message", "Unauthorized"));
      return;
    }
    String rawId = exchange.getRequestURI().getPath().replace("/api/users/", "");
    long userId = Long.parseLong(rawId);
    try (Connection connection = openConnection()) {
      if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
        Map<String, Object> user = getUser(connection, userId);
        if (user == null) {
          writeJson(exchange, 404, Map.of("message", "User not found"));
        } else {
          writeJson(exchange, 200, user);
        }
        return;
      }
      if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
        Map<String, String> payload = readJson(exchange, new TypeReference<>() {});
        updateUser(connection, userId, payload);
        writeJson(exchange, 200, getUser(connection, userId));
        return;
      }
      if ("PATCH".equalsIgnoreCase(exchange.getRequestMethod())) {
        Map<String, String> payload = readJson(exchange, new TypeReference<>() {});
        patchUser(connection, userId, payload);
        writeJson(exchange, 200, getUser(connection, userId));
        return;
      }
      if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
        deleteUser(connection, userId);
        writeJson(exchange, 200, Map.of("message", "Deleted", "userId", userId));
        return;
      }
      writeJson(exchange, 405, Map.of("message", "Method not allowed"));
    } catch (Exception exception) {
      writeJson(exchange, 500, Map.of("message", exception.getMessage()));
    }
  }

  private void handleUploadProcessing(HttpExchange exchange) throws IOException {
    if (!isAuthorized(exchange)) {
      writeJson(exchange, 401, Map.of("message", "Unauthorized"));
      return;
    }
    Map<String, String> payload = readJson(exchange, new TypeReference<>() {});
    String content = payload.getOrDefault("content", "");
    int rowCount =
        Math.max(
            0,
            content
                .lines()
                .skip(1)
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .toArray()
                .length);
    try (Connection connection = openConnection()) {
      long uploadId =
          insertUploadAudit(
              connection, payload.getOrDefault("fileName", "uploaded-file.csv"), rowCount);
      writeJson(
          exchange,
          200,
          Map.of("uploadId", uploadId, "rowCount", rowCount, "processingStatus", "PROCESSED"));
    } catch (Exception exception) {
      writeJson(exchange, 500, Map.of("message", exception.getMessage()));
    }
  }

  private void handleUploadLookup(HttpExchange exchange) throws IOException {
    if (!isAuthorized(exchange)) {
      writeJson(exchange, 401, Map.of("message", "Unauthorized"));
      return;
    }
    long uploadId = Long.parseLong(exchange.getRequestURI().getPath().replace("/api/uploads/", ""));
    try (Connection connection = openConnection()) {
      Map<String, Object> upload = getUploadAudit(connection, uploadId);
      if (upload == null) {
        writeJson(exchange, 404, Map.of("message", "Upload not found"));
      } else {
        writeJson(exchange, 200, upload);
      }
    } catch (Exception exception) {
      writeJson(exchange, 500, Map.of("message", exception.getMessage()));
    }
  }

  private boolean isAuthorized(HttpExchange exchange) {
    return ("Bearer " + TOKEN).equals(exchange.getRequestHeaders().getFirst("Authorization"));
  }

  private Connection openConnection() throws Exception {
    return DriverManager.getConnection(dbUrl, "sa", "");
  }

  private List<Map<String, Object>> listUsers(Connection connection) throws Exception {
    try (Statement statement = connection.createStatement();
        ResultSet resultSet =
            statement.executeQuery(
                "select user_id, name, email, role, status from managed_user order by user_id")) {
      List<Map<String, Object>> users = new ArrayList<>();
      while (resultSet.next()) {
        users.add(mapUser(resultSet));
      }
      return users;
    }
  }

  private Map<String, Object> getUser(Connection connection, long userId) throws Exception {
    try (PreparedStatement statement =
        connection.prepareStatement(
            "select user_id, name, email, role, status from managed_user where user_id = ?")) {
      statement.setLong(1, userId);
      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          return mapUser(resultSet);
        }
        return null;
      }
    }
  }

  private long insertUser(Connection connection, Map<String, String> payload) throws Exception {
    try (PreparedStatement statement =
        connection.prepareStatement(
            "insert into managed_user (name, email, role, status) values (?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, payload.get("name"));
      statement.setString(2, payload.get("email"));
      statement.setString(3, payload.get("role"));
      statement.setString(4, payload.get("status"));
      statement.executeUpdate();
      try (ResultSet keys = statement.getGeneratedKeys()) {
        keys.next();
        return keys.getLong(1);
      }
    }
  }

  private void updateUser(Connection connection, long userId, Map<String, String> payload)
      throws Exception {
    try (PreparedStatement statement =
        connection.prepareStatement(
            "update managed_user set name = ?, email = ?, role = ?, status = ? where user_id = ?")) {
      statement.setString(1, payload.get("name"));
      statement.setString(2, payload.get("email"));
      statement.setString(3, payload.get("role"));
      statement.setString(4, payload.get("status"));
      statement.setLong(5, userId);
      statement.executeUpdate();
    }
  }

  private void patchUser(Connection connection, long userId, Map<String, String> payload)
      throws Exception {
    Map<String, Object> existing = getUser(connection, userId);
    if (existing == null) {
      return;
    }
    try (PreparedStatement statement =
        connection.prepareStatement(
            "update managed_user set name = ?, email = ?, role = ?, status = ? where user_id = ?")) {
      statement.setString(1, payload.getOrDefault("name", String.valueOf(existing.get("name"))));
      statement.setString(2, payload.getOrDefault("email", String.valueOf(existing.get("email"))));
      statement.setString(3, payload.getOrDefault("role", String.valueOf(existing.get("role"))));
      statement.setString(
          4, payload.getOrDefault("status", String.valueOf(existing.get("status"))));
      statement.setLong(5, userId);
      statement.executeUpdate();
    }
  }

  private void deleteUser(Connection connection, long userId) throws Exception {
    try (PreparedStatement statement =
        connection.prepareStatement("delete from managed_user where user_id = ?")) {
      statement.setLong(1, userId);
      statement.executeUpdate();
    }
  }

  private long insertUploadAudit(Connection connection, String fileName, int rowCount)
      throws Exception {
    try (PreparedStatement statement =
        connection.prepareStatement(
            "insert into upload_audit (file_name, row_count, processing_status) values (?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, fileName);
      statement.setInt(2, rowCount);
      statement.setString(3, "PROCESSED");
      statement.executeUpdate();
      try (ResultSet keys = statement.getGeneratedKeys()) {
        keys.next();
        return keys.getLong(1);
      }
    }
  }

  private Map<String, Object> getUploadAudit(Connection connection, long uploadId)
      throws Exception {
    try (PreparedStatement statement =
        connection.prepareStatement(
            "select upload_id, file_name, row_count, processing_status from upload_audit where upload_id = ?")) {
      statement.setLong(1, uploadId);
      try (ResultSet resultSet = statement.executeQuery()) {
        if (!resultSet.next()) {
          return null;
        }
        Map<String, Object> upload = new HashMap<>();
        upload.put("uploadId", resultSet.getLong("upload_id"));
        upload.put("fileName", resultSet.getString("file_name"));
        upload.put("rowCount", resultSet.getInt("row_count"));
        upload.put("processingStatus", resultSet.getString("processing_status"));
        return upload;
      }
    }
  }

  private Map<String, Object> mapUser(ResultSet resultSet) throws Exception {
    Map<String, Object> user = new HashMap<>();
    user.put("userId", resultSet.getLong("user_id"));
    user.put("name", resultSet.getString("name"));
    user.put("email", resultSet.getString("email"));
    user.put("role", resultSet.getString("role"));
    user.put("status", resultSet.getString("status"));
    return user;
  }

  private <T> T readJson(HttpExchange exchange, TypeReference<T> targetType) throws IOException {
    try (InputStream inputStream = exchange.getRequestBody()) {
      return OBJECT_MAPPER.readValue(inputStream, targetType);
    }
  }

  private void writeJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
    byte[] payload = OBJECT_MAPPER.writeValueAsBytes(body);
    Headers headers = exchange.getResponseHeaders();
    headers.set("Content-Type", "application/json");
    exchange.sendResponseHeaders(statusCode, payload.length);
    try (OutputStream outputStream = exchange.getResponseBody()) {
      outputStream.write(payload);
    }
  }

  private void writeHtml(HttpExchange exchange, String html) throws IOException {
    byte[] payload = html.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
    exchange.sendResponseHeaders(200, payload.length);
    try (OutputStream outputStream = exchange.getResponseBody()) {
      outputStream.write(payload);
    }
  }

  private String loginPage() {
    return """
                <!DOCTYPE html>
                <html>
                <body>
                  <h1>Demo App Login</h1>
                  <input data-test='username' id='username' />
                  <input data-test='password' id='password' type='password' />
                  <button data-test='login-button' id='login-button'>Login</button>
                  <div data-test='login-error' id='login-error'></div>
                  <script>
                    document.getElementById('login-button').addEventListener('click', async () => {
                      const response = await fetch('/api/auth/login', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                          username: document.getElementById('username').value,
                          password: document.getElementById('password').value
                        })
                      });
                      const data = await response.json();
                      if (response.ok) {
                        localStorage.setItem('token', data.token);
                        window.location.href = '/users';
                      } else {
                        document.getElementById('login-error').innerText = data.message;
                      }
                    });
                  </script>
                </body>
                </html>
                """;
  }

  private String usersPage() {
    return """
                <!DOCTYPE html>
                <html>
                <body>
                  <h1>Managed Users</h1>
                  <div data-test='operation-message' id='operation-message'></div>
                  <section>
                    <input data-test='create-name' id='create-name' />
                    <input data-test='create-email' id='create-email' />
                    <input data-test='create-role' id='create-role' />
                    <input data-test='create-status' id='create-status' />
                    <button data-test='create-user' id='create-user'>Create User</button>
                  </section>
                  <section>
                    <input data-test='update-id' id='update-id' />
                    <input data-test='update-name' id='update-name' />
                    <input data-test='update-email' id='update-email' />
                    <input data-test='update-role' id='update-role' />
                    <input data-test='update-status' id='update-status' />
                    <button data-test='update-user' id='update-user'>Update User</button>
                  </section>
                  <section>
                    <input data-test='delete-id' id='delete-id' />
                    <button data-test='delete-user' id='delete-user'>Delete User</button>
                  </section>
                  <section>
                    <input data-test='upload-file' id='upload-file' type='file' />
                    <button data-test='process-upload' id='process-upload'>Process Upload</button>
                  </section>
                  <table>
                    <tbody data-test='user-table' id='user-table'></tbody>
                  </table>
                  <script>
                    const authHeaders = () => ({
                      'Content-Type': 'application/json',
                      'Authorization': 'Bearer ' + localStorage.getItem('token')
                    });
                    async function loadUsers() {
                      const response = await fetch('/api/users', { headers: authHeaders() });
                      const data = await response.json();
                      const table = document.getElementById('user-table');
                      table.innerHTML = '';
                      data.forEach(user => {
                        const row = document.createElement('tr');
                        row.setAttribute('data-test', 'user-row');
                        row.innerHTML = `<td data-test='user-id'>${user.userId}</td><td data-test='user-name'>${user.name}</td><td data-test='user-email'>${user.email}</td><td data-test='user-role'>${user.role}</td><td data-test='user-status'>${user.status}</td>`;
                        table.appendChild(row);
                      });
                    }
                    document.getElementById('create-user').addEventListener('click', async () => {
                      const response = await fetch('/api/users', {
                        method: 'POST',
                        headers: authHeaders(),
                        body: JSON.stringify({
                          name: document.getElementById('create-name').value,
                          email: document.getElementById('create-email').value,
                          role: document.getElementById('create-role').value,
                          status: document.getElementById('create-status').value
                        })
                      });
                      const data = await response.json();
                      document.getElementById('operation-message').innerText = response.ok ? `Created user ${data.userId}` : data.message;
                      await loadUsers();
                    });
                    document.getElementById('update-user').addEventListener('click', async () => {
                      const response = await fetch('/api/users/' + document.getElementById('update-id').value, {
                        method: 'PUT',
                        headers: authHeaders(),
                        body: JSON.stringify({
                          name: document.getElementById('update-name').value,
                          email: document.getElementById('update-email').value,
                          role: document.getElementById('update-role').value,
                          status: document.getElementById('update-status').value
                        })
                      });
                      const data = await response.json();
                      document.getElementById('operation-message').innerText = response.ok ? `Updated user ${data.userId}` : data.message;
                      await loadUsers();
                    });
                    document.getElementById('delete-user').addEventListener('click', async () => {
                      const id = document.getElementById('delete-id').value;
                      const response = await fetch('/api/users/' + id, { method: 'DELETE', headers: authHeaders() });
                      const data = await response.json();
                      document.getElementById('operation-message').innerText = response.ok ? data.message : data.message;
                      await loadUsers();
                    });
                    document.getElementById('process-upload').addEventListener('click', async () => {
                      const fileInput = document.getElementById('upload-file');
                      const file = fileInput.files[0];
                      const content = await file.text();
                      const response = await fetch('/api/uploads/process', {
                        method: 'POST',
                        headers: authHeaders(),
                        body: JSON.stringify({ fileName: file.name, content })
                      });
                      const data = await response.json();
                      document.getElementById('operation-message').innerText = response.ok ? `Processed upload ${data.uploadId}` : data.message;
                    });
                    loadUsers();
                  </script>
                </body>
                </html>
                """;
  }
}
