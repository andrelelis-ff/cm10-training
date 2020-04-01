package com.coremedia.ecommerce.studio.model {
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.beanFactory;

public class CatalogObjectTest extends AbstractCatalogModelTest {

  private var catalogObject:CatalogObject;

  override public function setUp():void {
    super.setUp();
    catalogObject = beanFactory.getRemoteBean("livecontext/store/HeliosSiteId/NO_WS") as CatalogObject;
  }

  public function testCatalogObject():void {
    (catalogObject as RemoteBean).load(addAsync(function ():void {
      assertEquals("PerfectChefESite", catalogObject.getName());
    }, 500));
  }

}
}