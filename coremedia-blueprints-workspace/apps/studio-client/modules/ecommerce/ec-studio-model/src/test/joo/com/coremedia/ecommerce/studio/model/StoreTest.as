package com.coremedia.ecommerce.studio.model {
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.beanFactory;

public class StoreTest extends AbstractCatalogModelTest {

  private var store:Store;

  override public function setUp():void {
    super.setUp();
    store = beanFactory.getRemoteBean("livecontext/store/HeliosSiteId/NO_WS") as Store;
  }

  public function testStore():void {
    (store as RemoteBean).load(addAsync(function ():void {
      assertEquals("PerfectChefESite", store.getName());
      assertEquals(2, store.getTopLevel().length);
      assertEquals("10851", store.getStoreId());
    }, 500));
  }
}
}