package com.coremedia.blueprint.studio.analytics {
import flexunit.framework.TestSuite;

public class TestSuite {
  public static function suite():flexunit.framework.TestSuite {
    var testSuite:flexunit.framework.TestSuite = new flexunit.framework.TestSuite();
    testSuite.addTestSuite(AnalyticsDeepLinkButtonContainerTest);
    testSuite.addTestSuite(OpenAnalyticsHomeUrlButtonTest);
    testSuite.addTestSuite(OpenAnalyticsDeepLinkUrlButtonTest);
    return testSuite;
  }
}
}