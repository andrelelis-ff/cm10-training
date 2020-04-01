package com.coremedia.livecontext.elastic.social.common;

import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductTransformerTest {

  @InjectMocks
  private ProductTransformer productTransformer = new ProductTransformer();

  @Mock
  private Product product;

  @Mock
  private SitesService sitesService;

  @Mock
  private StoreContext storeContext;

  @Mock
  private Site site;

  @Before
  public void setup() {
    String siteId = "1234";
    when(product.getContext()).thenReturn(storeContext);
    when(storeContext.getSiteId()).thenReturn(siteId);
    when(sitesService.findSite(siteId)).thenReturn(Optional.of(site));
  }

  @Test
  public void transform() {
    Object transformed = productTransformer.transform(product);
    assertThat(transformed).isNotNull();

    ProductInSite result = (ProductInSite) transformed;
    assertThat(result.getProduct()).isEqualTo(product);
    assertThat(result.getSite()).isEqualTo(site);
  }

  @Test
  public void getSite() {
    Site siteFromProductWrapper = productTransformer.getSite(product);

    assertThat(siteFromProductWrapper).isNotNull().isEqualTo(site);
  }

  @Test
  public void getType() {
    assertThat(productTransformer.getType()).isSameAs(Product.class);
  }
}
