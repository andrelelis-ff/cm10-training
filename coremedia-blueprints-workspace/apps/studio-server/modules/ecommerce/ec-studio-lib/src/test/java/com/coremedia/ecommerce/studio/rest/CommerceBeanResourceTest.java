package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.rest.cap.content.ContentRepositoryResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommerceBeanResourceTest {

  @Test
  public void computePreviewUrl() {
    Map<String, String> params = new HashMap<>();
    params.put(AbstractCatalogResource.PATH_SITE_ID, "_site_ID_");

    //noinspection unchecked
    CommerceBeanResource commerceBeanResource = mock(CommerceBeanResource.class);
    ContentRepositoryResource contentRepositoryResource = mock(ContentRepositoryResource.class);
    when(contentRepositoryResource.getPreviewControllerUrlPattern()).thenReturn("a={0}&b={1}");
    when(commerceBeanResource.getContentRepositoryResource()).thenReturn(contentRepositoryResource);

    CommerceBean commerceBean = mock(CommerceBean.class);
    when(commerceBeanResource.getEntity(params)).thenReturn(commerceBean);
    when(commerceBean.getId()).thenReturn(CommerceIdParserHelper.parseCommerceIdOrThrow("test:///x/product/_object_ID_"));
    when(commerceBeanResource.computePreviewUrl(params)).thenCallRealMethod();

    assertThat(commerceBeanResource.computePreviewUrl(params))
            .isEqualTo("a=test%3A%2F%2F%2Fcatalog%2Fproduct%2F_object_ID_&site=_site_ID_&b={1}");
  }
}
