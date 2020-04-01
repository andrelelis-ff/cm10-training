package com.coremedia.ecommerce.studio.components.repository {
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewConstants;
import com.coremedia.ecommerce.studio.AbstractCatalogStudioTest;
import com.coremedia.ecommerce.studio.helper.AugmentationUtil;
import com.coremedia.ecommerce.studio.model.Category;
import com.coremedia.ecommerce.studio.model.Product;
import com.coremedia.ui.data.beanFactory;

import ext.container.Viewport;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.ecommerce.studio.ECommerceStudioPlugin')]
public class CatalogRepositoryListTest extends AbstractCatalogStudioTest {
  private var category:Category;
  private var product:Product;
  private var viewport:Viewport;

  override public function setUp():void {
    super.setUp();
    category = beanFactory.getRemoteBean("livecontext/category/HeliosSiteId/catalog/NO_WS/Fruit") as Category;
    product = beanFactory.getRemoteBean("livecontext/product/HeliosSiteId/catalog/NO_WS/" + ORANGES_EXTERNAL_ID) as Product;

    viewport = new CatalogRepositoryListTestView();
  }

  override public function tearDown():void {
    super.tearDown();
    viewport.destroy();
  }

  public function testCatalogListType():void {
    var catalogList:CatalogRepositoryList = viewport.getComponent(CollectionViewConstants.LIST_VIEW) as CatalogRepositoryList;
    assertEquals(AugmentationUtil.getTypeLabel(category), ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'Category_label'));
    assertEquals(AugmentationUtil.getTypeLabel(product), ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'Product_label'));
  }

  public function testCatalogListTypeCls():void {
    var catalogList:CatalogRepositoryList = viewport.getComponent(CollectionViewConstants.LIST_VIEW) as CatalogRepositoryList;
    assertEquals(AugmentationUtil.getTypeCls(category), ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'Category_icon'));
    assertEquals(AugmentationUtil.getTypeCls(product), ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'Product_icon'));
  }
}
}