package com.automation.framework.tests.base;

import com.automation.framework.demoapp.DemoAppSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.ResourceLock;

@ResourceLock("demoApp")
public abstract class BaseDemoUiTest extends BaseUiTest {
  @BeforeEach
  void setUpDemoUi() {
    DemoAppSupport.reset();
  }

  @AfterEach
  void tearDownDemoUiOverrides() {
    DemoAppSupport.clearOverrides();
  }
}
