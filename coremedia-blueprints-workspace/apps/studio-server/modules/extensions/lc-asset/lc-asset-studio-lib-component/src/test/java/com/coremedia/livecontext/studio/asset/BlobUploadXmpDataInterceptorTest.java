package com.coremedia.livecontext.studio.asset;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextBuilderImpl;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.content.Content;
import com.coremedia.ecommerce.common.ProductIdExtractor;
import com.coremedia.ecommerce.test.TestVendors;
import com.coremedia.livecontext.asset.util.AssetHelper;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.activation.MimeType;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PrepareForTest({ProductIdExtractor.class})
@RunWith(PowerMockRunner.class)
public class BlobUploadXmpDataInterceptorTest {

  @Mock
  private CommerceConnectionSupplier commerceConnectionSupplier;

  @Mock
  private CommerceConnection commerceConnection;

  @Mock
  private CatalogService catalogService;

  @Mock
  private ContentWriteRequest contentWriteRequest;

  @Mock
  private Content parentFolder;

  @Mock
  private Content content;

  @Mock
  private Blob blob;

  @Mock
  private InputStream blobInputStream;

  @Mock
  private MimeType blobMimeType;

  @Mock
  private AssetHelper assetHelper;

  private Map<String, Object> properties;

  private BlobUploadXmpDataInterceptor testling;

  private StoreContext storeContext;

  @Before
  public void setup() {
    properties = new HashMap<>();

    when(commerceConnectionSupplier.findConnection(any(Content.class))).thenReturn(Optional.of(commerceConnection));

    testling = new BlobUploadXmpDataInterceptor(commerceConnectionSupplier, "data", assetHelper);

    when(blobMimeType.getPrimaryType()).thenReturn("image/jpeg");
    when(blob.getContentType()).thenReturn(blobMimeType);
    when(blob.getInputStream()).thenReturn(blobInputStream);

    storeContext = StoreContextBuilderImpl.from(commerceConnection, "any-site-id").build();

    when(commerceConnection.getStoreContext()).thenReturn(storeContext);
    when(commerceConnection.getCatalogService()).thenReturn(catalogService);
    when(commerceConnection.getIdProvider()).thenReturn(TestVendors.getIdProvider("vendor"));
  }

  @Test
  public void testInterceptNoMatch() {
    mockStatic(ProductIdExtractor.class);
    testling.intercept(contentWriteRequest);

    PowerMockito.verifyStatic(ProductIdExtractor.class, times(0));
    ProductIdExtractor.extractProductIds(blob);

    assertThat(properties).doesNotContainKey("localSettings");
  }

  @Test
  public void testInterceptNoXmpData() {
    properties.put("data", blob);

    when(contentWriteRequest.getProperties()).thenReturn(properties);
    when(contentWriteRequest.getParent()).thenReturn(parentFolder);
    mockStatic(ProductIdExtractor.class);
    when(ProductIdExtractor.extractProductIds(blob)).thenReturn(emptyList());

    testling.intercept(contentWriteRequest);

    assertThat(properties).containsEntry("localSettings", null);
  }

  @Test
  public void testInterceptWithXmpData() {
    properties.put("data", blob);

    when(contentWriteRequest.getProperties()).thenReturn(properties);
    when(contentWriteRequest.getParent()).thenReturn(parentFolder);
    when(contentWriteRequest.getEntity()).thenReturn(content);
    List<String> xmpData = Arrays.asList("PC_EVENING_DRESS", "PC_EVENING_DRESS-RED-M");
    mockStatic(ProductIdExtractor.class);
    when(ProductIdExtractor.extractProductIds(blob)).thenReturn(xmpData);
    when(assetHelper.updateCMPictureForExternalIds(content, xmpData)).thenReturn(null);

    Product productMock = mock(Product.class);
    CommerceId commerceId = CommerceIdParserHelper.parseCommerceIdOrThrow("vendor:///catalog/product/PC_EVENING_DRESS");
    when(productMock.getId()).thenReturn(commerceId);
    when(productMock.isVariant()).thenReturn(false);
    when(commerceConnection.getCatalogService().findProductById(commerceId, storeContext)).thenReturn(productMock);

    testling.intercept(contentWriteRequest);

    assertThat(properties).containsKey("localSettings");
  }

  @Test
  public void testRetrieveProductOrVariant() {
    String aProductExtId = "PC_EVENING_DRESS";
    String aSkuExtId = "PC_EVENING_DRESS-RED-M";
    String unknown = "unknown";
    String productId1 = "vendor:///catalog/product/" + aProductExtId;
    when(commerceConnection.getCatalogService().findProductById(CommerceIdParserHelper.parseCommerceIdOrThrow(productId1), storeContext)).thenReturn(mock(Product.class));
    String productVariantId1 = "vendor:///catalog/sku/" + aSkuExtId;
    when(commerceConnection.getCatalogService().findProductVariantById(CommerceIdParserHelper.parseCommerceIdOrThrow(productVariantId1), storeContext)).thenReturn(mock(ProductVariant.class));
    String productId2 = "vendor:///catalog/product/" + unknown;
    when(commerceConnection.getCatalogService().findProductById(CommerceIdParserHelper.parseCommerceIdOrThrow(productId2), storeContext)).thenReturn(null);
    String productVariantId2 = "vendor:///catalog/sku/" + unknown;
    when(commerceConnection.getCatalogService().findProductVariantById(CommerceIdParserHelper.parseCommerceIdOrThrow(productVariantId2), storeContext)).thenReturn(null);

    assertThat(testling.retrieveProductOrVariant(aProductExtId, commerceConnection)).isNotNull();
    assertThat(testling.retrieveProductOrVariant(aSkuExtId, commerceConnection)).isNotNull();
    assertThat(testling.retrieveProductOrVariant("unkown", commerceConnection)).isNull();
  }
}
