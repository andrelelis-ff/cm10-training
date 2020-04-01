package com.coremedia.ecommerce.studio {

import com.coremedia.ecommerce.studio.components.repository.CatalogRepositoryListTest;
import com.coremedia.ecommerce.studio.components.tree.AugmentedCategoryTreeRelationTest;
import com.coremedia.ecommerce.studio.components.tree.CatalogTreeModelTest;
import com.coremedia.ecommerce.studio.components.tree.ShowInLibraryHelperTest;
import com.coremedia.ecommerce.studio.helper.CatalogHelperTest;

import flexunit.framework.TestSuite;

public class TestSuite {
  public static function suite():flexunit.framework.TestSuite {
    var suite:flexunit.framework.TestSuite = new flexunit.framework.TestSuite();
    suite.addTestSuite(CatalogHelperTest);
    suite.addTestSuite(CatalogTreeModelTest);
    suite.addTestSuite(AugmentedCategoryTreeRelationTest);
    suite.addTestSuite(CatalogRepositoryListTest);
    suite.addTestSuite(ShowInLibraryHelperTest);
    return suite;
  }
}
}
