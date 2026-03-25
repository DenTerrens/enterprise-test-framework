package com.automation.framework.tests.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DemoUserDataFactory {
  private DemoUserDataFactory() {}

  public static Map<String, String> newUser(String prefix) {
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    Map<String, String> user = new HashMap<>();
    user.put("name", prefix + " User " + suffix);
    user.put("email", prefix.toLowerCase() + "." + suffix + "@demo.local");
    user.put("role", "ANALYST");
    user.put("status", "ACTIVE");
    return user;
  }

  public static Map<String, String> updatedUser(String prefix) {
    Map<String, String> user = newUser(prefix);
    user.put("role", "LEAD");
    user.put("status", "DISABLED");
    return user;
  }
}
