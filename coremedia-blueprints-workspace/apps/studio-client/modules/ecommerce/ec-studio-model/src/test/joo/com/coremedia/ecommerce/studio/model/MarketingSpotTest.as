package com.coremedia.ecommerce.studio.model {
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.beanFactory;

public class MarketingSpotTest extends AbstractCatalogModelTest {

  private var marketingSpot:MarketingSpot;

  override public function setUp():void {
    super.setUp();
    marketingSpot = beanFactory.getRemoteBean("livecontext/marketingspot/HeliosSiteId/NO_WS/spot1") as MarketingSpot;
  }

  public function testMarketingSpots():void {
    (marketingSpot as RemoteBean).load(addAsync(function ():void {
      assertEquals(true, marketingSpot.getMarketing() !== null);
    }, 500));
  }
}
}