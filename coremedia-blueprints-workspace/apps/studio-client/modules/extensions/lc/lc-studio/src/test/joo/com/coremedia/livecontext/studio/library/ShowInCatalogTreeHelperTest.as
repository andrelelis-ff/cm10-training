package com.coremedia.livecontext.studio.library {
import com.coremedia.cms.editor.sdk.collectionview.tree.CompoundChildTreeModel;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.model.Category;
import com.coremedia.ecommerce.studio.model.Store;
import com.coremedia.livecontext.studio.AbstractLiveContextStudioTest;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.beanFactory;

public class ShowInCatalogTreeHelperTest extends AbstractLiveContextStudioTest {

  public static var PREFERENCE_SHOW_CATALOG_KEY:String = "showCatalogContent";

  private var preferences:Bean;
  private var entities:Array;
  private var treeModel:CompoundChildTreeModel;
  private var showInCatalogTreeHelper:ShowInCatalogTreeHelper;
  private var category:Category;
  private var functionArguments:Array;


  override public function setUp():void {
    super.setUp();
    preferences = beanFactory.createLocalBean();
    editorContext['setPreferences'](preferences);
  }

  private function setUpCatalog():void {
    category = beanFactory.getRemoteBean("livecontext/category/HeliosSiteId/catalog/NO_WS/Fruit") as Category;
    entities = new Array(category);
    showInCatalogTreeHelper = new ShowInCatalogTreeHelper(entities);
    treeModel = ShowInCatalogTreeHelper.TREE_MODEL;
  }

  public function testSwitchSite():void {
    setUpCatalog();

    waitUntil("wait for store to load",
            function ():Boolean {
              return (CatalogHelper.getInstance().getActiveStoreExpression().getValue() as Store);
            },
            function ():void {
              waitUntil("wait for category to load",
                      function ():Boolean {
                        // wait for the complete path to be loaded otherwise it can not be opened in the catalog tree
                        return treeModel.getIdPathFromModel(category);
                      },
                      function ():void {
                        showInCatalogTreeHelper['adjustSettings'] = function (entity, callback, msg) {
                          functionArguments = [entity, callback, msg];
                        };
                        preferences.set(PREFERENCE_SHOW_CATALOG_KEY, false); // do not allow the catalog contents to be shown in content repository
                        // configure wrong site
                        editorContext.getSitesService().getPreferredSiteIdExpression().setValue("TestSiteId");

                        // test for the preferences: no catalog content visible in the repository tree
                        showInCatalogTreeHelper.showItems(treeModel.getTreeId());
                        assertNotNull(functionArguments);
                        assertEquals(entities[0], functionArguments[0]);
                        assertEquals(showInCatalogTreeHelper.showInCatalogTree, functionArguments[1]);
                      }
              );
            }
    );
  }

}
}