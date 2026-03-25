package com.automation.framework.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class RetrySupport {
  private RetrySupport() {}

  public static <T> T until(
      Supplier<T> action, Predicate<T> successCondition, Duration timeout, Duration pollInterval) {
    Instant deadline = Instant.now().plus(timeout);
    T lastResult = null;
    while (Instant.now().isBefore(deadline)) {
      lastResult = action.get();
      if (successCondition.test(lastResult)) {
        return lastResult;
      }
      sleep(pollInterval);
    }
    if (lastResult != null && successCondition.test(lastResult)) {
      return lastResult;
    }
    throw new IllegalStateException(
        "Condition was not satisfied within " + timeout.toMillis() + " ms");
  }

  private static void sleep(Duration duration) {
    try {
      Thread.sleep(duration.toMillis());
    } catch (InterruptedException interruptedException) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Retry polling interrupted", interruptedException);
    }
  }
}
