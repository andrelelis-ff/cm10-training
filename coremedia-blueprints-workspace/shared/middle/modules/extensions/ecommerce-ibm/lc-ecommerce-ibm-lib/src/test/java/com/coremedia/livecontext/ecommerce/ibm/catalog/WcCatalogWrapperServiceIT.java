package com.coremedia.livecontext.ecommerce.ibm.catalog;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.blueprint.lc.test.SwitchableHoverflyExtension;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.common.UnknownUserException;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractWrapperServiceTestCase;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmStoreContextBuilder;
import com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper;
import com.coremedia.livecontext.ecommerce.ibm.common.WcRestConnector;
import com.coremedia.livecontext.ecommerce.ibm.common.WcRestServiceMethod;
import com.coremedia.livecontext.ecommerce.ibm.pricing.WcPrice;
import com.coremedia.livecontext.ecommerce.ibm.pricing.WcPrices;
import com.coremedia.livecontext.ecommerce.ibm.storeinfo.StoreInfoService;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeanUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ReflectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Field;
import java.util.Currency;
import java.util.Optional;

import static com.coremedia.livecontext.ecommerce.ibm.catalog.IbmCatalogServiceBaseTest.IBM_TEST_URL;
import static com.coremedia.livecontext.ecommerce.ibm.catalog.WcCatalogWrapperService.FIND_CATEGORY_BY_EXTERNAL_ID;
import static com.coremedia.livecontext.ecommerce.ibm.catalog.WcCatalogWrapperService.FIND_CATEGORY_BY_EXTERNAL_ID_SEARCH;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_7_6;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_7_7;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_8_0;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({SwitchableHoverflyExtension.class, SpringExtension.class})
@HoverflySimulate(
        source = @HoverflySimulate.Source(
                "wcs8_WcCatalogWrapperServiceIT.json"
        ),
        // Re-Record as soon as source file is not available.
        enableAutoCapture = true,
        config = @HoverflyConfig(
                // map the "shop-ref.ecommerce.coremedia.com" to an existing ip of a wcs system in your /etc/hosts file
                destination = IBM_TEST_URL,
                disableTlsVerification = true
        )
)
@ActiveProfiles({IbmServiceTestBase.LocalConfig.PROFILE})
public class WcCatalogWrapperServiceIT extends AbstractWrapperServiceTestCase {

  private static final String PRODUCT_EXTERNAL_ID = System.getProperty("lc.test.product.externalId", "CLA022_2203");

  private static final Currency CURRENCY_EUR = Currency.getInstance("EUR");

  @Inject
  private WcCatalogWrapperService testling;

  @Inject
  @Named("commerce:wcs1")
  private CommerceConnection connection;

  @Inject
  protected StoreInfoService storeInfoService;

  private StoreContextImpl storeContext;

  @BeforeEach
  void setup() {
    storeInfoService.getWcsVersion().ifPresent(testConfig::setWcsVersion);
    if (WCS_VERSION_8_0.lessThan(testConfig.getWcsVersion())) {
      String searchServiceSslEndpoint = wcRestConnector.getSearchServiceSslEndpoint(storeContext).replace("previewresources", "resources");
      wcRestConnector.setSearchServiceSslEndpoint(searchServiceSslEndpoint);
    }
    storeContext = testConfig.getStoreContext(connection);
  }

  @Test
  void testFindDynamicProductPriceByExternalId() {
    WcPrice productPrice = testling.findDynamicProductPriceByExternalId(PRODUCT_EXTERNAL_ID, storeContext, null);
    assertNotNull(productPrice.getPriceValue());
    if (StoreContextHelper.getWcsVersion(storeContext).lessThan(WCS_VERSION_7_7)) {
      assertNotNull(productPrice.getPriceDescription());
      assertNotNull(productPrice.getPriceUsage());
    } else {
      assertNotNull(productPrice.getCurrency());
    }
    assertEquals(storeContext.getCurrency().getCurrencyCode(), productPrice.getCurrency());

    // test with different currency (CMS-9402)
    productPrice = testling.findDynamicProductPriceByExternalId(PRODUCT_EXTERNAL_ID,
            testConfig.getStoreContext(connection, CURRENCY_EUR), null);
    assertNotNull(productPrice.getPriceValue());
    assertEquals("EUR", productPrice.getCurrency());
  }

  @Test
  void testFindStaticProductPricesByExternalId() {
    UserContext userContext = null;

    WcPrices productPrice = testling
            .findStaticProductPricesByExternalId(PRODUCT_EXTERNAL_ID, null, storeContext, userContext);
    assertNotNull(productPrice);
    assertTrue(productPrice.getPrices().containsKey("Offer"));
    if (WCS_VERSION_7_7 == StoreContextHelper.getWcsVersion(storeContext)) {
      assertTrue(productPrice.getPrices().containsKey("Display"));
    }
  }

  @Test
  void testUnknownUser() {
    String userName = "mr.unknown"; // Should be unknown.
    if (StoreContextHelper.getWcsVersion(storeContext).lessThan(WCS_VERSION_7_7)) {
      throw new UnknownUserException(userName, 401);
    }

    UserContext userContext = UserContext.builder().withUserName(userName).build();
    Assertions.assertThrows(UnknownUserException.class, () -> {
      testling.findDynamicProductPriceByExternalId(PRODUCT_EXTERNAL_ID, storeContext, userContext);
    });
  }

  @Test
  void testUseSearchRestHandler() {
    assertFalse(testling.useSearchRestHandlerProduct(storeContext));

    testling.setUseSearchRestHandlerProductIfAvailable(true);
    assertTrue(testling.useSearchRestHandlerProduct(storeContext));

    StoreContextImpl storeContextWcs76 = IbmStoreContextBuilder
            .from(storeContext)
            .withWcsVersion(WCS_VERSION_7_6)
            .build();
    assertFalse(testling.useSearchRestHandlerProduct(storeContextWcs76));
  }

  @Test
  void testMixedModeCategory() {
    WcCatalogWrapperService testlingSpy = spy(testling);
    // desired setup
    testlingSpy.setUseSearchRestHandlerProductIfAvailable(true);
    testlingSpy.setUseSearchRestHandlerCategoryIfAvailable(false);

    WcRestConnector restConnector = new WcRestConnector();
    BeanUtils.copyProperties(testlingSpy.getRestConnector(), restConnector);
    testlingSpy.getRestConnector();
    WcRestConnector restConnectorSpy = spy(restConnector);

    // mock call for product in order to verify endpoint called
    doReturn(Optional.empty()).when(restConnectorSpy).callService(
            any(WcRestServiceMethod.class),
            anyList(),
            anyMap(),
            any(),
            any(StoreContext.class),
            nullable(UserContext.class));
    testlingSpy.setRestConnector(restConnectorSpy);

    UserContext userContext = null;

    testlingSpy.findCategoryByExternalId("vanilla", null, storeContext, userContext);

    // BOD handler called
    verify(restConnectorSpy, times(1)).callService(eq((WcRestServiceMethod) FIND_CATEGORY_BY_EXTERNAL_ID),
            anyList(),
            anyMap(),
            any(),
            any(StoreContext.class),
            nullable(UserContext.class));

    // and not search handler
    verify(restConnectorSpy, never()).callService(eq((WcRestServiceMethod) FIND_CATEGORY_BY_EXTERNAL_ID_SEARCH),
            anyList(),
            anyMap(),
            any(),
            any(StoreContext.class),
            nullable(UserContext.class));

    reset(testlingSpy, restConnectorSpy);
  }

  @Test
  void testMixedModeProduct() throws Exception {
    WcCatalogWrapperService testlingSpy = spy(testling);
    // desired setup
    testlingSpy.setUseSearchRestHandlerProductIfAvailable(true);
    testlingSpy.setUseSearchRestHandlerCategoryIfAvailable(false);

    Field productRestCallFieldSearch = ReflectionUtils.findField(testlingSpy.getClass(), "FIND_PRODUCT_BY_EXTERNAL_ID_SEARCH");
    productRestCallFieldSearch.setAccessible(true);
    Field productRestCallFieldBod = ReflectionUtils.findField(testlingSpy.getClass(), "FIND_PRODUCT_BY_EXTERNAL_ID");
    productRestCallFieldBod.setAccessible(true);
    WcRestConnector restConnector = testlingSpy.getRestConnector();
    WcRestConnector restConnectorSpy = spy(restConnector);

    // mock call for product in order to verify endpoint called
    doReturn(Optional.empty()).when(restConnectorSpy).callService(
            any(WcRestServiceMethod.class),
            anyList(),
            anyMap(),
            any(),
            any(StoreContext.class),
            nullable(UserContext.class));
    testlingSpy.setRestConnector(restConnectorSpy);

    UserContext userContext = null;

    testlingSpy.findProductByExternalId("0815", null, storeContext, userContext);

    // search handler called
    verify(restConnectorSpy, times(1)).callService(eq((WcRestServiceMethod) productRestCallFieldSearch.get(null)),
            anyList(),
            anyMap(),
            any(),
            any(StoreContext.class),
            nullable(UserContext.class));

    // and not BOD handler
    verify(restConnectorSpy, never()).callService(eq((WcRestServiceMethod) productRestCallFieldBod.get(null)),
            anyList(),
            anyMap(),
            any(),
            any(StoreContext.class),
            nullable(UserContext.class));

    reset(testlingSpy, restConnectorSpy);
  }
}
