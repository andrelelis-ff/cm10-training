package com.coremedia.ecommerce.studio.components.tree {
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.collectionview.tree.CompoundChildTreeModel;
import com.coremedia.cms.editor.sdk.collectionview.tree.ContentTreeModel;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ecommerce.studio.AbstractCatalogStudioTest;
import com.coremedia.ecommerce.studio.components.tree.impl.CatalogTreeModel;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.library.ShowInLibraryHelper;
import com.coremedia.ecommerce.studio.model.Category;
import com.coremedia.ecommerce.studio.model.Store;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.test.Step;

public class ShowInLibraryHelperTest extends AbstractCatalogStudioTest {

  public static var PREFERENCE_SHOW_CATALOG_KEY:String = "showCatalogContent";

  private var entities:Array;
  private var treeModel:CompoundChildTreeModel;
  private var showInLibraryHelper:ShowInLibraryHelper;
  private var category:Category;
  private var augmentedCategory:Content;
  private var functionArguments:Array;

  private var preferences:Bean;

  override public function setUp():void {
    super.setUp();
    preferences = beanFactory.createLocalBean();
    editorContext['setPreferences'](preferences);
  }

  private function setUpCatalog():void {
    treeModel = new CatalogTreeModel();
    category = beanFactory.getRemoteBean("livecontext/category/HeliosSiteId/catalog/NO_WS/Fruit") as Category;
    entities = new Array(category);
    showInLibraryHelper = new ShowInLibraryHelper(entities, treeModel);
  }

  private function setUpRepository() {
    treeModel = new ContentTreeModel();
    augmentedCategory = beanFactory.getRemoteBean("content/500") as Content; // Catalog Root
    entities = new Array(augmentedCategory);
    showInLibraryHelper = new ShowInLibraryHelper(entities, treeModel);
  }

  internal function checkOpenedInCatalog():void {
    editorContext['getCollectionViewManager'] = function () {
      return {
        showInRepository: function (entity, view, treeModelId) {
          functionArguments = [entity, view, treeModelId];
        }
      }
    };
    // do not allow the catalog contents to be shown in content repository
    preferences.set(PREFERENCE_SHOW_CATALOG_KEY, false);

    // test for the preferences: no catalog content visible in the repository tree
    showInLibraryHelper.showItems(treeModel.getTreeId());
    assertNotNull(functionArguments);
    assertEquals(entities[0], functionArguments[0]);
    assertEquals(null, functionArguments[1]);
    assertEquals(treeModel.getTreeId(), functionArguments[2]);
  }

  internal function checkOpenInContentRepository():void {
    editorContext['getCollectionViewManager'] = function () {
      return {
        showInRepository: function (entity, view, treeModelId) {
          functionArguments = [entity, view, treeModelId];
        }
      }
    };
    // allow the catalog contents to be shown in content repository
    preferences.set(PREFERENCE_SHOW_CATALOG_KEY, true);

    // test for the preferences: no catalog content visible in the repository tree
    showInLibraryHelper.showItems(treeModel.getTreeId());
    assertNotNull(functionArguments);
    assertEquals(entities[0], functionArguments[0]);
    assertEquals(null, functionArguments[1]);
    assertEquals(treeModel.getTreeId(), functionArguments[2]);
  }

  private function makeSiteInvalid():void {
    // make shop invalid and still open the item in the given tree
    editorContext.getSitesService().getPreferredSiteIdExpression().setValue("TestSiteId");
  }

  private function waitForActiveStoreLoadStep():Step {
    return new Step(
            "wait for store to load",
            function():Boolean {
              return (CatalogHelper.getInstance().getActiveStoreExpression().getValue() is Store);
            }
    );
  }

  private function waitForCategoryLoadStep():Step {
    return new Step(
            "wait for category to load",
            function ():Boolean {
              // wait for the complete path to be loaded otherwise it can not be opened in the catalog tree
              return treeModel.getIdPathFromModel(category);
            }
    );
  }

  private function waitUntilStoreIsLoadedStep():Step {
    return new Step(
            "wait for store to load again",
            function ():Boolean {
              // wait for the complete path to be loaded otherwise it can not be opened in the catalog tree
              return (CatalogHelper.getInstance().getActiveStoreExpression().getValue() is Store);
            },
            checkOpenedInCatalog
    );
  }

  public function testShowInCatalogTreeWithSteps() {
    chain(
            stepFromFunction(setUpCatalog, "set up catalog"),
            waitForActiveStoreLoadStep(),
            waitForCategoryLoadStep(),
            stepFromFunction(checkOpenedInCatalog, "check open in catalog")
    );
  }

  public function testShowInContentRepositoryTreeWithSteps() {
    chain(
            stepFromFunction(setUpRepository, "set up repository"),
            waitForActiveStoreLoadStep(),
            stepFromFunction(checkOpenInContentRepository, "check open in content repository")
    );
  }

  public function testMyWrongSiteWithSteps():void {
    chain(
            stepFromFunction(setUpCatalog, "set up catalog"),
            waitForActiveStoreLoadStep(),
            waitForCategoryLoadStep(),
            stepFromFunction(makeSiteInvalid, "make site invalid"),
            waitForActiveStoreLoadStep(),
            stepFromFunction(checkOpenedInCatalog, "check open in catalog")
    );
  }

  private function stepFromFunction(callback:Function, msg:String):Step {
    return new Step(
            msg,
            function ():Boolean {
              return true;
            },
            callback
    );
  }
}
}