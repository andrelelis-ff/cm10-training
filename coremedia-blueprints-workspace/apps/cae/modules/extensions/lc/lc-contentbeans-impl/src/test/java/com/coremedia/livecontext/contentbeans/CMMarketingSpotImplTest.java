package com.coremedia.livecontext.contentbeans;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdUtils;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.ContentSiteAspect;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType;
import com.coremedia.livecontext.ecommerce.common.CommerceBeanFactory;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.common.Vendor;
import com.coremedia.livecontext.ecommerce.p13n.MarketingSpot;
import com.coremedia.livecontext.ecommerce.p13n.MarketingSpotService;
import com.coremedia.livecontext.navigation.LiveContextNavigationFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CMMarketingSpotImplTest {

  private static final String MY_MARKETING_SPOT_NAME = "myMarketingSpotName";

  @Mock
  private CommerceBeanFactory commerceBeanFactory;

  @Mock
  private Content content;

  @Mock
  private MarketingSpot marketingSpot;

  @Mock
  private MarketingSpotService marketingSpotService;

  @Mock
  private LiveContextNavigationFactory liveContextNavigationFactory;

  @Mock
  private CommerceConnectionSupplier commerceConnectionSupplier;

  @Mock
  private SitesService sitesService;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private StoreContext storeContext;

  @Mock
  private ContentSiteAspect contentSiteAspect;

  @Mock
  private Site site;

  private CMMarketingSpotImpl testling;

  @Before
  public void init() {
    initMocks(this);

    CommerceConnection commerceConnection = mock(CommerceConnection.class);
    when(commerceConnection.getStoreContext()).thenReturn(storeContext);
    when(storeContext.getConnection().getVendor()).thenReturn(Vendor.of("moin"));
    when(commerceConnection.getCommerceBeanFactory()).thenReturn(commerceBeanFactory);
    when(commerceConnection.getMarketingSpotService()).thenReturn(Optional.of(marketingSpotService));

    when(commerceConnectionSupplier.findConnection(any(Content.class))).thenReturn(Optional.of(commerceConnection));

    CommerceId externalId = CommerceIdUtils.builder(BaseCommerceBeanType.MARKETING_SPOT, storeContext)
            .withExternalId("myExternalId")
            .build();
    when(marketingSpotService.findMarketingSpotById(eq(externalId), any(StoreContext.class))).thenReturn(marketingSpot);
    when(marketingSpot.getName()).thenReturn(MY_MARKETING_SPOT_NAME);

    when(content.getString(CMMarketingSpotImpl.EXTERNAL_ID)).thenReturn(CommerceIdFormatterHelper.format(externalId));

    testling = new TestCMMarketingSpotImpl();
    testling.setSitesService(sitesService);
    testling.setLiveContextNavigationFactory(liveContextNavigationFactory);
    testling.setCommerceConnectionSupplier(commerceConnectionSupplier);
    when(sitesService.getContentSiteAspect(any(Content.class))).thenReturn(contentSiteAspect);
    when(contentSiteAspect.findSite()).thenReturn(Optional.of(site));
  }

  @Test
  public void testGetTeaserTitle() {
    //teaser title is set
    String myTeaserTitle = "myTeaserTitle";
    when(content.getString(CMTeasable.TEASER_TITLE)).thenReturn(myTeaserTitle);
    assertThat(testling.getTeaserTitle())
            .as("the teaser title of the CMMarketingSpot is the same as the teaserTitle string property of the content")
            .isEqualTo(myTeaserTitle);

    //teaser title is not set
    when(content.getString(CMTeasable.TEASER_TITLE)).thenReturn(null);
    assertThat(testling.getTeaserTitle())
            .as("the teaser title of the CMMarketingSpot is the same as the teaserTitle string property of the content")
            .isEqualTo(MY_MARKETING_SPOT_NAME);
  }

  @Test
  public void testGetMarketingSpot() {
    MarketingSpot marketingSpot = testling.getMarketingSpot();
    assertThat(marketingSpot).isNotNull();
    assertThat(marketingSpot.getName()).isEqualTo(MY_MARKETING_SPOT_NAME);
  }

  @Test
  public void testGetMarketingSpotIdNotValid() {
    when(content.getString(CMMarketingSpotImpl.EXTERNAL_ID)).thenReturn("test:///me/marketingspot/notFound");
    assertThat(testling.getMarketingSpot()).isNull();
  }

  private class TestCMMarketingSpotImpl extends CMMarketingSpotImpl {
    @Override
    public Content getContent() {
      return content;
    }
  }
}
