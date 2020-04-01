package com.coremedia.ecommerce.studio.components.tree {
import com.coremedia.cms.editor.sdk.EditorContextImpl;
import com.coremedia.ecommerce.studio.AbstractCatalogStudioTest;
import com.coremedia.ecommerce.studio.components.tree.impl.CatalogTreeModel;
import com.coremedia.ui.models.NodeChildren;
import com.coremedia.ui.models.TreeModel;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.ecommerce.studio.ECommerceStudioPlugin')]
public class CatalogTreeModelTest extends AbstractCatalogStudioTest {

  private var catalogTreeModel:TreeModel;

  override public function setUp():void {
    super.setUp();

    EditorContextImpl.initEditorContext();

    //noinspection BadExpressionStatementJS
    catalogTreeModel = new CatalogTreeModel();
  }

  public function testGetStoreText():void {
    waitUntil("wait for store text",
      function():Boolean {
        return catalogTreeModel.getText(STORE_ID);
      },
      function():void {
        assertEquals(catalogTreeModel.getText(STORE_ID), STORE_NAME);
      }
    );
  }

  public function testGetTopCategoryText():void {
    waitUntil("wait for the top category loaded",
      function():Boolean {
        return catalogTreeModel.getText(TOP_CATEGORY_ID);
      },
      function():void {
        assertEquals(catalogTreeModel.getText(TOP_CATEGORY_ID), TOP_CATEGORY_EXTERNAL_ID);
      }
    );
  }

  public function testGetLeafCategoryText():void {
    waitUntil("wait for the leaf category loaded",
      function():Boolean {
        return catalogTreeModel.getText(LEAF_CATEGORY_ID);
      },
      function():void {
        assertEquals(catalogTreeModel.getText(LEAF_CATEGORY_ID), LEAF_CATEGORY_EXTERNAL_ID);
      }
    );
  }

  public function testGetTopCategoryIdPath():void {
    waitUntil("wait for the top categories loaded",
      function():Boolean {
        var idPaths:Array = catalogTreeModel.getIdPath(TOP_CATEGORY_ID) as Array;
        return idPaths && idPaths.length === 3;
      },
      function():void {
        var idPaths:Array = catalogTreeModel.getIdPath(TOP_CATEGORY_ID) as Array;
        assertEquals(idPaths[0], STORE_ID);
        assertEquals(idPaths[1], ROOT_CATEGORY_ID);
        assertEquals(idPaths[2], TOP_CATEGORY_ID);
      }
    );
  }

  public function testGetLeafCategoryIdPath():void {
    waitUntil("wait for leaf category id path",
      function():Boolean {
        return catalogTreeModel.getIdPath(LEAF_CATEGORY_ID);
      },
      function():void {
        var idPaths:Array = catalogTreeModel.getIdPath(LEAF_CATEGORY_ID) as Array;
        assertEquals(4, idPaths.length);
        assertEquals(STORE_ID, idPaths[0]);
        assertEquals(ROOT_CATEGORY_ID, idPaths[1]);
        assertEquals(TOP_CATEGORY_ID, idPaths[2]);
        assertEquals(LEAF_CATEGORY_ID, idPaths[3]);
      }
    );
  }

  public function testGetTopCategoryChildren():void {
    waitUntil("wait for top category children",
      function():Boolean {
        return catalogTreeModel.getChildren(TOP_CATEGORY_ID);
      },
      function():void {
        var nodeChildren:NodeChildren = catalogTreeModel.getChildren(TOP_CATEGORY_ID);
        assertEquals(nodeChildren.getChildIds().length, 2);
      }

    );

  }

  public function testGetLeafCategoryChildren():void {
    waitUntil("wait for leaf category children",
      function():Boolean {
        return catalogTreeModel.getChildren(LEAF_CATEGORY_ID);
      },
      function():void {
        var nodeChildren:NodeChildren = catalogTreeModel.getChildren(LEAF_CATEGORY_ID);
        assertEquals(nodeChildren.getChildIds().length, 0);
      }
    );
  }

  public function testGetStoreChildren():void {
    waitUntil("wait for store children",
      function():Boolean {
        return catalogTreeModel.getChildren(STORE_ID);
      },
      function():void {
        var topLevelIds:Array = catalogTreeModel.getChildren(STORE_ID).getChildIds();
        assertEquals(topLevelIds.length, 2);
        assertEquals(topLevelIds[0], MARKETING_ID);
        assertEquals(topLevelIds[1], ROOT_CATEGORY_ID);
      }
    );
  }

  public function testGetMarketingSpotsText():void {
    waitUntil("wait for tree to be build",
      function():Boolean {
        return catalogTreeModel.getText(MARKETING_ID);
      },
      function():void {
        assertEquals(catalogTreeModel.getText(MARKETING_ID), ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'StoreTree_marketing_root'));
      }
    );
  }

  public function testGetRootId():void {
    waitUntil("wait for root id to be loaded",
      function ():Boolean {
        return catalogTreeModel.getRootId() === STORE_ID;
      },
      function ():void {
        assertEquals(catalogTreeModel.getRootId(), STORE_ID);
      }
    );
  }

  public function testGetStoreIdPath():void {
    waitUntil("wait for store id path",
      function():Boolean {
        return catalogTreeModel.getIdPath(STORE_ID);
      },
      function():void {
        var idPaths:Array = catalogTreeModel.getIdPath(STORE_ID);
        assertEquals(idPaths.length, 1);
        assertEquals(idPaths[0], STORE_ID);
      }
    );
  }
}
}