package com.coremedia.ecommerce.studio.components.tree {
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.EditorContextImpl;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.SitesService;
import com.coremedia.ecommerce.studio.AbstractCatalogStudioTest;
import com.coremedia.ecommerce.studio.model.CategoryImpl;
import com.coremedia.ecommerce.studio.tree.augmentedCategoryTreeRelation;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.test.Step;

public class AugmentedCategoryTreeRelationTest extends AbstractCatalogStudioTest {

  private var siteRootDocument:RemoteBean;
  private var rootCategoryDocument:RemoteBean;
  private var topCategory:CategoryImpl;
  private var topCategoryDocument:RemoteBean;
  private var leafCategoryDocument:RemoteBean;

  internal var editorContext_getSitesService:Function;

  override public function setUp():void {
    super.setUp();

    EditorContextImpl.initEditorContext();

    var editorContext:EditorContextImpl = EditorContextImpl.getInstance();
    editorContext_getSitesService = editorContext.getSitesService;
    editorContext.getSitesService = getSitesService;

    siteRootDocument = beanFactory.getRemoteBean(SITE_ROOT_DOCUMENT_ID);
    rootCategoryDocument = beanFactory.getRemoteBean(ROOT_CATEGORY_DOCUMENT_ID);
    topCategory = beanFactory.getRemoteBean(TOP_CATEGORY_ID) as CategoryImpl;
    topCategoryDocument = beanFactory.getRemoteBean(TOP_CATEGORY_DOCUMENT_ID);
    leafCategoryDocument = beanFactory.getRemoteBean(LEAF_CATEGORY_DOCUMENT_ID);
  }


  override public function tearDown():void {
    super.tearDown();
    editorContext.getSitesService = editorContext_getSitesService;
  }

  public function testGetChildrenOfLeafCategoryDocument():void {
    //TODO: nothing to test as augmentedCategoryTreeRelation#getChildrenOf is not implemented yet.
  }

  public function testGetParentUncheckedOfLeafCategoryDocument():void {
    chain(
            waitForTheUncheckedParentOfLeafCategoryDocumentToBeRootCategoryDocument(),
            augmentTopCategory(),
            waitForTheUncheckedParentOfLeafCategoryDocumentToBeTopCategoryDocument()
    )
  }

  private function waitForTheUncheckedParentOfLeafCategoryDocumentToBeRootCategoryDocument():Step {
    return new Step("wait for the unchecked parent of the leaf category document to be the category root document",
            function ():Boolean {
              return rootCategoryDocument === augmentedCategoryTreeRelation.getParentUnchecked(leafCategoryDocument);
            });
  }

  private function augmentTopCategory():Step {
    return new Step("Augment the top category",
            function ():Boolean {return true;},
            function ():void {
              topCategory.getContent = function():RemoteBean {
                return topCategoryDocument;
              }
            });
  }

  private function waitForTheUncheckedParentOfLeafCategoryDocumentToBeTopCategoryDocument():Step {
    return new Step("wait for the unchecked parent of the leaf category document to be the top category document",
            function ():Boolean {
              return topCategoryDocument === augmentedCategoryTreeRelation.getParentUnchecked(leafCategoryDocument);
            });
  }

  public function testIsRootOfSiteRootDocument():void {
    waitUntil("wait for the site root document to be evaluated to be root",
            function ():Boolean {
              return augmentedCategoryTreeRelation.isRoot(siteRootDocument);
            }
    );

  }

  public function testIsNotRootOfRootCategoryDocument():void {
    waitUntil("wait for the root category document to be evaluated not to be root",
            function ():Boolean {
              return !augmentedCategoryTreeRelation.isRoot(rootCategoryDocument);
            }
    );

  }

  private function getSitesService():SitesService {
    return SitesService({
      'getSiteRootDocument': function (siteId:String):* {
        return siteRootDocument;
      },
      'getSiteIdFor': function (content:Content):String {
        return HELIOS_SITE_ID;
      }
    });
  }


}
}