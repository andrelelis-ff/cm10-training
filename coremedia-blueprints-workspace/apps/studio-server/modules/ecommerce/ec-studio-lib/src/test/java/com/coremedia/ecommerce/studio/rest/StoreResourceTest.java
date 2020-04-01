package com.coremedia.ecommerce.studio.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StoreResourceTest {

  @InjectMocks
  private StoreResource storeResource;

  @Mock
  private PbeShopUrlTargetResolver pbeShopUrlTargetResolver;

  @Before
  public void setup() {
    storeResource.setPbeShopUrlTargetResolvers(singletonList(pbeShopUrlTargetResolver));
  }

  @Test
  public void handlePostNoData() {
    Map<String, Object> rawJson = emptyMap();
    assertThat(storeResource.handlePost(emptyMap(), rawJson)).isNull();
  }

  @Test
  public void handlePost() {
    Object o = new Object();
    String url = "http://test.net";
    when(pbeShopUrlTargetResolver.resolveUrl(url, null)).thenReturn(o);
    Map<String, Object> rawJson = singletonMap("shopUrl", url);
    assertThat(storeResource.handlePost(emptyMap(), rawJson)).isSameAs(o);
  }
}
