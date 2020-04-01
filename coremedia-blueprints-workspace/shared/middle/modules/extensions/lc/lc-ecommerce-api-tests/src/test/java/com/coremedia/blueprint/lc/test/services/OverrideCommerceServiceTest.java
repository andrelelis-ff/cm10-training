package com.coremedia.blueprint.lc.test.services;

import com.coremedia.blueprint.base.livecontext.client.common.GenericCommerceConnection;
import com.coremedia.blueprint.base.livecontext.client.common.GenericCommerceConnectionFactory;
import com.coremedia.blueprint.base.livecontext.client.common.RequiresGenericCommerceConnection;
import com.coremedia.blueprint.base.livecontext.client.config.CommerceAdapterClientAutoConfiguration;
import com.coremedia.blueprint.base.livecontext.client.data.DataClient;
import com.coremedia.blueprint.base.livecontext.client.data.DataClientFactory;
import com.coremedia.blueprint.base.livecontext.client.settings.CommerceSettings;
import com.coremedia.blueprint.base.livecontext.client.settings.CommerceSettingsProvider;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceServicesAutoConfiguration;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.Vendor;
import com.coremedia.livecontext.ecommerce.event.InvalidationService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SpringBootTest(properties = {
        "repository.factoryClassName=com.coremedia.cap.xmlrepo.XmlCapConnectionFactory",
        "livecontext.cache.invalidation.enabled=false",
}, classes = {
        BaseCommerceServicesAutoConfiguration.class,
        CommerceAdapterClientAutoConfiguration.class
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OverrideCommerceServiceTest {

  @SpyBean
  private SettingsService settingsService;

  @MockBean
  private DataClientFactory dataClientFactory;

  @MockBean
  private CatalogAliasTranslationService catalogAliasTranslationService;

  @MockBean(extraInterfaces = {
          TestCommerceService.class,
  })
  private CatalogService testCatalogService;

  @MockBean(extraInterfaces = {
          TestCommerceService.class,
          RequiresGenericCommerceConnection.class
  })
  private InvalidationService testInvalidationService;

  @Autowired
  private GenericCommerceConnectionFactory connectionFactory;

  @Mock
  private CommerceSettingsProvider commerceSettingsProvider;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private CommerceSettings commerceSettings;

  @Mock
  private Site site;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private DataClient dataClient;

  private GenericCommerceConnection connection;

  @BeforeAll
  void setup() {
    when(site.getId()).thenReturn("siteId");
    when(site.getLocale()).thenReturn(Locale.CANADA_FRENCH);

    when(settingsService.createProxy(CommerceSettingsProvider.class, site)).thenReturn(commerceSettingsProvider);
    when(commerceSettingsProvider.getCommerce()).thenReturn(commerceSettings);

    when(commerceSettings.getEndpoint()).thenReturn("testEndpoint");
    when(commerceSettings.getCatalogConfig().getId()).thenReturn("testCatalogId");
    when(dataClientFactory.createDataClient("testEndpoint")).thenReturn(Optional.of(dataClient));

    when(dataClient.getMetadata().getVendor()).thenReturn(Vendor.of("test"));
    connection = connectionFactory.createConnection(site).orElseThrow(IllegalStateException::new);
  }

  @Test
  void testOverride() {
    assertThat(connection).satisfies(
            genericCommerceConnection -> {
              // precondition for custom service lookup
              assertThat(genericCommerceConnection).returns("test", CommerceConnection::getVendorName);

              // custom catalog service instance was found
              assertThat(genericCommerceConnection).returns(testCatalogService, CommerceConnection::getCatalogService);

              // the custom invalidation service was asked to provide a new instance
              verify(((RequiresGenericCommerceConnection) testInvalidationService), times(1))
                      .setGenericCommerceConnection(genericCommerceConnection);
            });
  }

}
