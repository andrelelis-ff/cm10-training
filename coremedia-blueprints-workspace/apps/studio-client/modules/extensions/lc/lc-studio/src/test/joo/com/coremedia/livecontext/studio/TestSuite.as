package com.coremedia.livecontext.studio {

import com.coremedia.livecontext.studio.action.CollectionViewModelActionTest;
import com.coremedia.livecontext.studio.collectionview.CatalogCollectionViewTest;
import com.coremedia.livecontext.studio.components.link.CatalogLinkPropertyFieldTest;
import com.coremedia.livecontext.studio.forms.ProductTeaserDocumentFormTest;
import com.coremedia.livecontext.studio.forms.ProductTeaserSettingsFormTest;
import com.coremedia.livecontext.studio.library.ShowInCatalogTreeHelperTest;

import flexunit.framework.TestSuite;

public class TestSuite {
  public static function suite():flexunit.framework.TestSuite {
    var suite:flexunit.framework.TestSuite = new flexunit.framework.TestSuite();

    suite.addTestSuite(CatalogCollectionViewTest);
    suite.addTestSuite(CollectionViewModelActionTest);
    suite.addTestSuite(CatalogLinkPropertyFieldTest);
    suite.addTestSuite(ProductTeaserDocumentFormTest);
    suite.addTestSuite(ShowInCatalogTreeHelperTest);
    suite.addTestSuite(ProductTeaserSettingsFormTest);

    return suite;
  }
}
}
