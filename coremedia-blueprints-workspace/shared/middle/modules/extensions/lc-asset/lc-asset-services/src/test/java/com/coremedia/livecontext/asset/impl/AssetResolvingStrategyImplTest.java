package com.coremedia.livecontext.asset.impl;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextBuilderImpl;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.asset.AssetSearchService;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper.parseCommerceIdOrThrow;
import static com.google.common.collect.ImmutableList.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetResolvingStrategyImplTest {

  private static final String EXTERNAL_ID = "externalId1";
  private static final String EXTERNAL_ID_SKU = "externalIdSKU";
  private static final String COMMERCE_ID_REF = "vendor:///catalog/product/" + EXTERNAL_ID;
  private static final CommerceId COMMERCE_ID = parseCommerceIdOrThrow(COMMERCE_ID_REF);
  private static final String COMMERCE_ID_SKU_REF = "vendor:///catalog/sku/" + EXTERNAL_ID_SKU;
  private static final CommerceId COMMERCE_ID_SKU = parseCommerceIdOrThrow(COMMERCE_ID_SKU_REF);

  private static final String CMPICTURE_DOCTYPE_NAME = "CMPicture";

  @Spy
  @InjectMocks
  private AssetResolvingStrategyImpl testling;

  @Mock
  private AssetSearchService assetSearchService;

  @Mock
  private AssetChanges assetChanges;

  @Mock
  private Site site;

  @Mock
  private CommerceConnectionInitializer commerceConnectionInitializer;

  @Mock
  private CatalogService catalogService;

  private StoreContext storeContext;

  @Before
  public void setUp() {
    CommerceConnection commerceConnection = mock(CommerceConnection.class);
    storeContext = StoreContextBuilderImpl.from(commerceConnection, "any-site-id").build();

    when(commerceConnection.getStoreContext()).thenReturn(storeContext);
    when(commerceConnection.getCatalogService()).thenReturn(catalogService);

    when(commerceConnectionInitializer.findConnectionForSite(any(Site.class)))
            .thenReturn(Optional.of(commerceConnection));
  }

  @Test
  public void noCacheOneIndexedInSolrButNotUpToDate() {
    Content picture = createPictureMock("picture");

    List<Content> indexedAssets = of(picture);
    List<Content> cachedAssets = of();

    returnIndexedAssets(EXTERNAL_ID, indexedAssets);
    returnCachedAssets(COMMERCE_ID_REF, site, cachedAssets);
    isUpToDateInCache(picture, COMMERCE_ID_REF, site, false);

    List<?> assets = testling.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID, site);
    assertTrue(assets.isEmpty());
  }

  @Test
  public void twoCachedAndIndexedOneNotUptodate() {
    String upToDatePictureName = "picture up to date in cache";
    Content pictureUpToDate = createPictureMock(upToDatePictureName);
    Content otherPicture = createPictureMock("not up to date in cache");

    List<Content> indexedAssets = of(otherPicture, pictureUpToDate);
    List<Content> cachedAssets = of(otherPicture, pictureUpToDate);
    List<String> referencedOnContent = of(EXTERNAL_ID);

    returnIndexedAssets(EXTERNAL_ID, indexedAssets);
    returnCachedAssets(EXTERNAL_ID, site, cachedAssets);
    isUpToDateInCache(otherPicture, EXTERNAL_ID, site, false);
    isUpToDateInCache(pictureUpToDate, EXTERNAL_ID, site, true);
    isReferencedInContent(pictureUpToDate, referencedOnContent);

    List<?> assets = testling.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID, site);
    assertEquals(1, assets.size());
    assertEquals(upToDatePictureName, ((Content) assets.get(0)).getName());
  }

  @Test
  public void oneCachedAndNotIndexed() {
    String pictureName = "picture";
    Content picture = createPictureMock(pictureName);

    List<Content> indexedAssets = of();
    List<Content> cachedAssets = of(picture);
    List<String> externalIdsOnContent = of(EXTERNAL_ID);

    returnIndexedAssets(EXTERNAL_ID, indexedAssets);
    returnCachedAssets(EXTERNAL_ID, site, cachedAssets);
    isUpToDateInCache(picture, EXTERNAL_ID, site, true);
    isReferencedInContent(picture, externalIdsOnContent);

    List<?> assets = testling.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID, site);

    assertEquals(1, assets.size());
    assertEquals(pictureName, ((Content) assets.get(0)).getName());
  }

  @Test
  public void twoCachedOneIndexedOnlyOneOnContent() {
    String aPicturesName = "picture one";
    String anotherPicturesName = "picture two";
    Content aPicture = createPictureMock(aPicturesName);
    Content anotherPicture = createPictureMock(anotherPicturesName);

    List<Content> indexedAssets = of(aPicture);
    List<Content> cachedAssets = of(aPicture, anotherPicture);
    List<String> externalIdsOnContent = of(EXTERNAL_ID);

    returnIndexedAssets(EXTERNAL_ID, indexedAssets);
    returnCachedAssets(EXTERNAL_ID, site, cachedAssets);

    isUpToDateInCache(aPicture, EXTERNAL_ID, site, true);
    isReferencedInContent(aPicture, externalIdsOnContent);

    isUpToDateInCache(anotherPicture, EXTERNAL_ID, site, true);
    isReferencedInContent(anotherPicture, externalIdsOnContent);

    List<?> assets = testling.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID, site);

    assertEquals(2, assets.size());
    assertEquals(aPicturesName, ((Content) assets.get(0)).getName());
    assertEquals(anotherPicturesName, ((Content) assets.get(1)).getName());
  }

  @Test
  public void findProductAssetsOneIndexedAndCachedAndReferenced() {
    Content picture = createPictureMock("picture");
    List<Content> indexedAssets = of(picture);
    List<Content> cachedAssets = of(picture);
    List<String> externalIdsOnContent = of(EXTERNAL_ID);

    returnIndexedAssets(EXTERNAL_ID, indexedAssets);
    returnCachedAssets(EXTERNAL_ID, site, cachedAssets);
    isUpToDateInCache(picture, EXTERNAL_ID, site, true);
    isReferencedInContent(picture, externalIdsOnContent);

    Collection<?> pictures = testling.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID, site);
    assertEquals(1, pictures.size());
  }

  @Test
  public void findProductAssetsNonInCacheOrIndexed() {
    List<Content> indexedAssets = of();
    List<Content> cachedAssets = of();

    returnIndexedAssets(EXTERNAL_ID, indexedAssets);
    returnCachedAssets(EXTERNAL_ID, site, cachedAssets);

    Collection<?> pictures = testling.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID, site);
    assertTrue(pictures.isEmpty());
  }

  @Test
  public void testFindAssetsForVariants() {
    Content variantPicture = createPictureMock("variant picture");
    Content productPicture = createPictureMock("product picture");

    List<Content> indexedVariantAssets = of(variantPicture);
    List<Content> indexedProductAssets = of(productPicture);
    List<Content> cachedProductAssets = of(productPicture);
    List<String> referencedOnProductContent = of(EXTERNAL_ID);

    returnProductVariantWithProduct(COMMERCE_ID_SKU, COMMERCE_ID_REF);
    returnIndexedAssets(EXTERNAL_ID_SKU, indexedVariantAssets);

    returnIndexedAssets(EXTERNAL_ID, indexedProductAssets);
    returnCachedAssets(EXTERNAL_ID, site, cachedProductAssets);
    isUpToDateInCache(productPicture, EXTERNAL_ID, site, true);
    isReferencedInContent(productPicture, referencedOnProductContent);

    Collection<?> pictures = testling.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID_SKU, site);
    assertEquals(1, pictures.size());
  }

  @Test
  public void findAssetsForSKUsWithFallbackToParent() {
    Content variantPicture = createPictureMock("variant picture");
    Content productPicture = createPictureMock("product picture");

    List<Content> indexedVariantAssets = of(variantPicture);
    List<Content> cachedProductAssets = of(productPicture);
    List<Content> indexedProductAssets = of(productPicture);
    List<String> referencedOnContent = of(EXTERNAL_ID);

    returnProductVariantWithProduct(COMMERCE_ID_SKU, COMMERCE_ID_REF);
    returnIndexedAssets(EXTERNAL_ID_SKU, indexedVariantAssets);

    returnCachedAssets(EXTERNAL_ID, site, cachedProductAssets);
    returnIndexedAssets(EXTERNAL_ID, indexedProductAssets);
    isUpToDateInCache(productPicture, EXTERNAL_ID, site, true);
    isReferencedInContent(productPicture, referencedOnContent);

    Collection<?> pictures = testling.findAssets(CMPICTURE_DOCTYPE_NAME, COMMERCE_ID_SKU, site);
    assertEquals(1, pictures.size());
  }

  private void isUpToDateInCache(Content asset, String externalId, Site site, boolean isUpToDate) {
    when(assetChanges.isUpToDate(asset, externalId, site)).thenReturn(isUpToDate);
  }

  private void returnCachedAssets(String externalId, Site site, List<Content> assets) {
    when(assetChanges.get(externalId, site)).thenReturn(assets);
  }

  private void isReferencedInContent(Content picture, List<String> referencesFromCommerceStruct) {
    doReturn(referencesFromCommerceStruct).when(testling).getExternalIds(picture);
  }

  private void returnIndexedAssets(String externalId, List<Content> picturesInSolrForExternalId) {
    when(assetSearchService.searchAssets(CMPICTURE_DOCTYPE_NAME, externalId, site))
            .thenReturn(picturesInSolrForExternalId);
  }

  private void returnProductVariantWithProduct(CommerceId productVariantId, String productId) {
    ProductVariant variant = mock(ProductVariant.class);
    when(catalogService.findProductById(productVariantId, storeContext)).thenReturn(variant);
    markAsSKU(productVariantId);

    Product product = mock(Product.class);
    when(product.getReference()).thenReturn(parseCommerceIdOrThrow(productId));

    when(variant.getParent()).thenReturn(product);
  }

  private Content createPictureMock(String pictureName) {
    Content picture = mock(Content.class);
    ContentType contentType = mock(ContentType.class);
    when(contentType.isSubtypeOf(CMPICTURE_DOCTYPE_NAME)).thenReturn(true);
    when(picture.getName()).thenReturn(pictureName);
    when(picture.getType()).thenReturn(contentType);
    return picture;
  }

  private void markAsSKU(CommerceId id) {
    doReturn(true).when(testling).isSkuId(id);
  }
}
