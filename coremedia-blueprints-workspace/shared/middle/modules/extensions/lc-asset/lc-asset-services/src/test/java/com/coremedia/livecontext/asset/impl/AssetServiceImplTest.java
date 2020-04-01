package com.coremedia.livecontext.asset.impl;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextBuilderImpl;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.ecommerce.test.TestVendors;
import com.coremedia.livecontext.ecommerce.asset.CatalogPicture;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.link.LinkService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper.parseCommerceIdOrThrow;
import static com.google.common.collect.ImmutableList.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetServiceImplTest {

  private static final String EXTERNAL_ID = "externalId1";
  private static final String EXTERNAL_ID_SKU = "externalIdSKU";

  private static final CommerceId COMMERCE_ID
          = parseCommerceIdOrThrow("vendor:///catalog/product/" + EXTERNAL_ID);

  private static final CommerceId COMMERCE_ID_SKU_AS_PRODUCT
          = parseCommerceIdOrThrow("vendor:///catalog/product/" + EXTERNAL_ID_SKU);

  private static final String URL_WITH_EXTERNAL_ID_1
          = "http://localhost:40081/blueprint/servlet/catalogimage/product/10202/en_US/full/" + EXTERNAL_ID + ".jpg";

  private static final String URL_WITH_EXTERNAL_ID_OF_SKU
          = "http://localhost:40081/blueprint/servlet/catalogimage/product/10202/en_US/full/" + EXTERNAL_ID_SKU + ".jpg";

  private static final String NOT_LINKED_URL
          = "http://localhost:40081/blueprint/servlet/catalogimage/product/10202/en_US/full/anyID.jpg";

  private static final String COMMERCE_URL
          = "http://shop-preview-production-helios.blueprint-box.vagrant/wcsstore/ExtendedSitesCatalogAssetStore/images/catalog/apparel/boys/bcl014_tops/646x1000/bcl014_1417.jpg";

  private static final String CMPICTURE_DOCTYPE_NAME = "CMPicture";

  @InjectMocks
  @Spy
  private AssetServiceImpl testling;

  @Mock
  private SitesService sitesService;

  @Mock
  private SettingsService settingsService;

  @Mock
  private AssetResolvingStrategy assetResolvingStrategy;

  @Mock
  private Site site1;

  @Mock
  private LinkService linkService;

  @Before
  public void setUp() {
    CommerceConnection commerceConnection = mock(CommerceConnection.class);

    StoreContextImpl storeContext = StoreContextBuilderImpl.from(commerceConnection, "site-1").build();

    when(commerceConnection.getIdProvider()).thenReturn(TestVendors.getIdProvider("vendor"));
    when(commerceConnection.getLinkService()).thenReturn(Optional.of(linkService));

    CurrentStoreContext.set(storeContext);

    testling.setAssetResolvingStrategy(assetResolvingStrategy);

    when(commerceConnection.getLinkService().get().getImageUrl(anyString(), any(StoreContext.class)))
            .thenReturn(Optional.of("http://an/asset/url.jpg"));
  }

  @After
  public void tearDown() {
    CurrentStoreContext.remove();
  }

  @SuppressWarnings("Duplicates")
  @Test
  public void testGetCatalogPicture() {
    when(sitesService.findSite(anyString())).thenReturn(Optional.of(site1));
    Content picture = mock(Content.class);
    when(assetResolvingStrategy.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID, site1)).thenReturn(of(picture));

    CatalogPicture catalogPicture = testling.getCatalogPicture(URL_WITH_EXTERNAL_ID_1);
    assertNotNull(catalogPicture);
    assertEquals(picture, catalogPicture.getPicture());
  }

  @SuppressWarnings("Duplicates")
  @Test
  public void testGetCatalogPictureFromCommerceId() {
    when(sitesService.findSite(anyString())).thenReturn(Optional.of(site1));
    Content picture = mock(Content.class);
    when(assetResolvingStrategy.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID, site1)).thenReturn(of(picture));

    CatalogPicture catalogPicture = testling.getCatalogPicture(URL_WITH_EXTERNAL_ID_1, COMMERCE_ID);
    assertNotNull(catalogPicture);
    assertEquals(picture, catalogPicture.getPicture());
  }

  @SuppressWarnings("Duplicates")
  @Test
  public void testGetCatalogPictureSKU() {
    when(sitesService.findSite(anyString())).thenReturn(Optional.of(site1));
    Content picture = mock(Content.class);
    when(assetResolvingStrategy.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID_SKU_AS_PRODUCT, site1)).thenReturn(of(picture));

    CatalogPicture catalogPicture = testling.getCatalogPicture(URL_WITH_EXTERNAL_ID_OF_SKU);
    assertNotNull(catalogPicture);
    assertEquals(picture, catalogPicture.getPicture());
  }

  @SuppressWarnings("Duplicates")
  @Test
  public void testGetCatalogPictureSKUFromCommerceId() {
    when(sitesService.findSite(anyString())).thenReturn(Optional.of(site1));
    Content picture = mock(Content.class);
    when(assetResolvingStrategy.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID_SKU_AS_PRODUCT, site1)).thenReturn(of(picture));

    CatalogPicture catalogPicture = testling.getCatalogPicture(URL_WITH_EXTERNAL_ID_OF_SKU, COMMERCE_ID_SKU_AS_PRODUCT);
    assertNotNull(catalogPicture);
    assertEquals(picture, catalogPicture.getPicture());
  }

  @Test
  public void testGetCatalogPicture2() {
    CatalogPicture catalogPicture = testling.getCatalogPicture(COMMERCE_URL);
    assertNotNull(catalogPicture);
    assertNull(catalogPicture.getPicture());
    assertNotNull(catalogPicture.getUrl());
  }

  @Test
  public void testGetCatalogPictureSiteDefault() {
    when(sitesService.findSite(anyString())).thenReturn(Optional.of(site1));
    StoreContext storeContext = CurrentStoreContext.get();
    when(linkService.getImageUrl(NOT_LINKED_URL, storeContext)).thenReturn(Optional.of(NOT_LINKED_URL));

    CatalogPicture catalogPicture = testling.getCatalogPicture(NOT_LINKED_URL);
    assertNull(catalogPicture.getPicture());
    assertEquals(NOT_LINKED_URL, catalogPicture.getUrl());
  }

  @Test
  public void testDefaultPicture() {
    Content defaultPicture = mock(Content.class);

    when(settingsService.getSetting(anyString(), eq(Content.class), nullable(Content.class)))
            .thenReturn(Optional.of(defaultPicture));
    when(sitesService.findSite(anyString())).thenReturn(Optional.of(site1));

    assertEquals(defaultPicture, testling.findPictures(COMMERCE_ID).iterator().next());
    assertEquals(defaultPicture, testling.findPictures(COMMERCE_ID, true).iterator().next());
    assertTrue(testling.findPictures(COMMERCE_ID, false).isEmpty());
  }
}
