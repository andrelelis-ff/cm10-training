package com.coremedia.ecommerce.studio {

import com.coremedia.ecommerce.studio.model.CatalogObjectTest;
import com.coremedia.ecommerce.studio.model.CategoryTest;
import com.coremedia.ecommerce.studio.model.MarketingSpotTest;
import com.coremedia.ecommerce.studio.model.MarketingTest;
import com.coremedia.ecommerce.studio.model.ProductTest;
import com.coremedia.ecommerce.studio.model.StoreTest;

import flexunit.framework.TestSuite;

public class TestSuite {
  public static function suite():flexunit.framework.TestSuite {
    var suite:flexunit.framework.TestSuite = new flexunit.framework.TestSuite();

    suite.addTestSuite(CatalogObjectTest);
    suite.addTestSuite(StoreTest);
    suite.addTestSuite(MarketingTest);
    suite.addTestSuite(MarketingSpotTest);
    suite.addTestSuite(CategoryTest);
    suite.addTestSuite(ProductTest);
    return suite;
  }
}
}
