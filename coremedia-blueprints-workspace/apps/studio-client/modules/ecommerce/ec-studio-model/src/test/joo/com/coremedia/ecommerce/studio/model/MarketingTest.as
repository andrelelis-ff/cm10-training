package com.coremedia.ecommerce.studio.model {
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.beanFactory;

public class MarketingTest extends AbstractCatalogModelTest {

  private var marketing:Marketing;

  override public function setUp():void {
    super.setUp();
    marketing = beanFactory.getRemoteBean("livecontext/marketing/HeliosSiteId/NO_WS") as Marketing;
  }

  public function testMarketing():void {
    (marketing as RemoteBean).load(addAsync(function ():void {
      assertEquals(3, marketing.getMarketingSpots().length);
    }, 500));
  }
}
}