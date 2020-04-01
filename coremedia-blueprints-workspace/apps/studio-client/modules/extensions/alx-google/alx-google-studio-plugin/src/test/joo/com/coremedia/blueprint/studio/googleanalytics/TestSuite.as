package com.coremedia.blueprint.studio.googleanalytics {
import flexunit.framework.TestSuite;

public class TestSuite {
  public static function suite():flexunit.framework.TestSuite {
    var testSuite:flexunit.framework.TestSuite = new flexunit.framework.TestSuite();
    testSuite.addTestSuite(GoogleAnalyticsStudioButtonTest);
    return testSuite;
  }
}
}