package com.coremedia.livecontext.product;

import com.coremedia.blueprint.base.links.ContentLinkBuilder;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.cae.handlers.NavigationSegmentsUriHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.ecommerce.test.TestVendors;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductVariant;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceIdProvider;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriTemplate;

import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENTS_FRAGMENT;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link com.coremedia.livecontext.product.ProductAvailabilityHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductAvailabilityHandlerTest {

  private static final String ANY_VANITY_NAME = "any vanity name";
  private static final String PRODUCT_AVAILABILITY_PREFIX
          = '/' + PREFIX_DYNAMIC + '/' + SEGMENTS_FRAGMENT + "/productavailability/";

  @InjectMocks
  private ProductAvailabilityHandler testling;

  @Mock
  private CatalogService catalogService;

  @Mock
  private ContentLinkBuilder contentLinkBuilder;

  @Mock
  private NavigationSegmentsUriHelper navigationSegmentsUriHelper;

  private final CommerceIdProvider idProvider = TestVendors.getIdProvider("test");

  @Before
  public void setUp() {
    CommerceConnection commerceConnection = mock(CommerceConnection.class);
    StoreContext storeContext = mock(StoreContext.class);

    when(commerceConnection.getCatalogService()).thenReturn(catalogService);
    when(commerceConnection.getIdProvider()).thenReturn(idProvider);

    when(storeContext.getConnection()).thenReturn(commerceConnection);

    CurrentStoreContext.set(storeContext);
  }

  @After
  public void teardown() {
    CurrentStoreContext.remove();
  }

  @Test
  public void testBuildLinkForProduct() {
    UriTemplate uriTemplate = mock(UriTemplate.class);
    when(uriTemplate.toString()).thenReturn(ProductAvailabilityHandler.URI_PATTERN);

    when(contentLinkBuilder.getVanityName(nullable(Content.class))).thenReturn(ANY_VANITY_NAME);

    Product product = mock(Product.class);
    when(product.getExternalId()).thenReturn("0815");

    ProductInSite productInSite = mock(ProductInSite.class);
    when(productInSite.getProduct()).thenReturn(product);
    when(productInSite.getSite()).thenReturn(mock(Site.class));

    UriComponents uriComponents = testling.buildLinkFor(productInSite, uriTemplate, null);

    assertEquals("Expected link does not match built link.",
            PRODUCT_AVAILABILITY_PREFIX + ANY_VANITY_NAME + "/product/0815", uriComponents.getPath());
  }

  @Test
  public void testBuildLinkForProductVariant() {
    UriTemplate uriTemplate = mock(UriTemplate.class);
    when(uriTemplate.toString()).thenReturn(ProductAvailabilityHandler.URI_PATTERN);

    when(contentLinkBuilder.getVanityName(nullable(Content.class))).thenReturn(ANY_VANITY_NAME);

    ProductVariant product = mock(ProductVariant.class);
    when(product.getExternalId()).thenReturn("0815");

    ProductInSite productInSite = mock(ProductInSite.class);
    when(productInSite.getProduct()).thenReturn(product);
    when(productInSite.getSite()).thenReturn(mock(Site.class));

    UriComponents uriComponents = testling.buildLinkFor(productInSite, uriTemplate, null);

    assertEquals("Expected link does not match built link.",
            PRODUCT_AVAILABILITY_PREFIX + ANY_VANITY_NAME + "/variant/0815", uriComponents.getPath());
  }

  @Test
  public void testHandleDynamicRequestProduct() {
    Product product = mock(Product.class);

    when(catalogService.findProductById(any(), any(StoreContext.class))).thenReturn(product);
    ModelAndView modelAndView = testling
            .handleDynamicFragmentRequest("anyShopName", "product", "0815", "availabilityFragment");

    assertEquals("availabilityFragment", modelAndView.getViewName());
    assertEquals(product, modelAndView.getModel().get("self"));
  }

  @Test
  public void testHandleDynamicRequestProductVariant() {
    ProductVariant product = mock(ProductVariant.class);

    when(catalogService.findProductVariantById(any(), any(StoreContext.class))).thenReturn(product);
    ModelAndView modelAndView = testling
            .handleDynamicFragmentRequest("anyShopName", "variant", "0815_v", "availabilityFragment");

    assertEquals("availabilityFragment", modelAndView.getViewName());
    assertEquals(product, modelAndView.getModel().get("self"));
  }
}
