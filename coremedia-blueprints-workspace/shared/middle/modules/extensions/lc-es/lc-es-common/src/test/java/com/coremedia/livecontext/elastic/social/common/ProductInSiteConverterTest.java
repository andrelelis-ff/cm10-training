package com.coremedia.livecontext.elastic.social.common;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.ecommerce.test.TestVendors;
import com.coremedia.elastic.core.api.models.UnresolvableReferenceException;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceIdProvider;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.livecontext.elastic.social.common.ProductInSiteConverter.ID;
import static com.coremedia.livecontext.elastic.social.common.ProductInSiteConverter.SITE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductInSiteConverterTest {

  private String productId = "1234";
  private String productReferenceId = "vendor:///catalog/product/" + productId;
  private String siteId = "5678";

  @Mock
  private Product product;

  @Mock
  private ProductInSite productInSite;

  @Mock
  private CommerceConnectionInitializer commerceConnectionInitializer;

  @Mock
  private Site site;

  @Mock
  private SitesService sitesService;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private CommerceConnection commerceConnection;

  private final CommerceIdProvider idProvider = TestVendors.getIdProvider("vendor");

  private ProductInSiteConverter converter;

  @Before
  public void setup() {
    when(commerceConnection.getIdProvider()).thenReturn(idProvider);
    when(commerceConnectionInitializer.findConnectionForSite(site)).thenReturn(Optional.of(commerceConnection));

    when(commerceConnection.getCatalogService().findProductById(any(), any(StoreContext.class))).thenReturn(product);

    when(product.getExternalId()).thenReturn(productId);

    when(productInSite.getProduct()).thenReturn(product);
    when(productInSite.getSite()).thenReturn(site);

    when(product.getId()).thenReturn(CommerceIdParserHelper.parseCommerceIdOrThrow(productReferenceId));

    when(sitesService.findSite(siteId)).thenReturn(Optional.of(site));

    when(site.getId()).thenReturn(siteId);

    converter = new ProductInSiteConverter(sitesService, commerceConnectionInitializer);
  }

  @Test
  public void getType() {
    assertThat(converter.getType()).isEqualTo(ProductInSite.class);
  }

  @Test
  public void serializeWithProductReferenceId() {
    Map<String, Object> serializedObject = new HashMap<>();

    converter.serialize(productInSite, serializedObject);

    assertThat(serializedObject.entrySet()).hasSize(2);
    assertThat(serializedObject.get(ID)).isEqualTo(productReferenceId);
    assertThat(serializedObject.get(SITE_ID)).isEqualTo(siteId);
  }

  @Test
  public void deserialize() {
    Map<String, Object> serializedObject = new HashMap<>();
    serializedObject.put("id", productReferenceId);
    serializedObject.put("site", siteId);

    ProductInSite result = converter.deserialize(serializedObject);

    assertThat(result.getProduct()).isSameAs(product);
    assertThat(result.getSite()).isSameAs(site);
  }

  @Test(expected = UnresolvableReferenceException.class)
  public void deserializeUnresolvable() {
    Map<String, Object> serializedObject = new HashMap<>();
    serializedObject.put("id", "id");
    serializedObject.put("site", "site");

    converter.deserialize(serializedObject);
  }
}
