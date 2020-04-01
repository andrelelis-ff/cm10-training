package com.coremedia.livecontext.elastic.social.common;

import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductInSiteTransformerTest {

  @InjectMocks
  private ProductInSiteTransformer productTransformer = new ProductInSiteTransformer();

  @Mock
  private ProductInSite productInSite;

  @Mock
  private Site site;

  @Test
  public void transform() {
    Object transformed = productTransformer.transform(productInSite);
    assertThat(transformed).isNotNull();

    ProductInSite result = (ProductInSite) transformed;
    assertThat(result).isSameAs(productInSite);
  }

  @Test
  public void getSite() {
    when(productInSite.getSite()).thenReturn(site);

    Site siteFromProductWrapper = productTransformer.getSite(productInSite);

    assertThat(siteFromProductWrapper).isNotNull().isEqualTo(site);
  }

  @Test
  public void getType() {
    assertThat(productTransformer.getType()).isSameAs(ProductInSite.class);
  }
}
