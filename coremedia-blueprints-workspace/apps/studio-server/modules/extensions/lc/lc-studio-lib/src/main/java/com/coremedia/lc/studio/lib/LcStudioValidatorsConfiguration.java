package com.coremedia.lc.studio.lib;

import com.coremedia.blueprint.base.links.UrlPathFormattingHelper;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceServicesAutoConfiguration;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.rest.validators.ChannelNavigationValidator;
import com.coremedia.blueprint.base.rest.validators.ChannelReferrerValidator;
import com.coremedia.blueprint.base.rest.validators.ChannelSegmentValidator;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.lc.studio.lib.validators.CatalogLinkValidator;
import com.coremedia.lc.studio.lib.validators.ExternalChannelValidator;
import com.coremedia.lc.studio.lib.validators.ExternalPagePartOfNavigationValidator;
import com.coremedia.lc.studio.lib.validators.ExternalPageValidator;
import com.coremedia.lc.studio.lib.validators.ExternalProductValidator;
import com.coremedia.lc.studio.lib.validators.IsLiveContextTypeValidator;
import com.coremedia.lc.studio.lib.validators.SegmentFormatValidator;
import com.coremedia.rest.cap.validators.StructLinkListIndexValidator;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Configuration
@Import({BaseCommerceServicesAutoConfiguration.class})
@ImportResource(value = {
        "classpath:/META-INF/coremedia/lc-services.xml",
        "classpath:/com/coremedia/cache/cache-services.xml",
        "classpath:/com/coremedia/blueprint/base/multisite/bpbase-multisite-services.xml",
        "classpath:/com/coremedia/blueprint/base/links/bpbase-urlpathformatting.xml"
}, reader = ResourceAwareXmlBeanDefinitionReader.class)
public class LcStudioValidatorsConfiguration {

  private static final String CMPRODUCTLIST_NAME = "CMProductList";
  private static final String CMEXTERNALCHANNEL_NAME = "CMExternalChannel";
  private static final String EXTERNALID_PROPERTY = "externalId";

  private static CatalogLinkValidator createCatalogLinkValidator(CapConnection connection,
                                                                 CommerceConnectionInitializer commerceConnectionInitializer,
                                                                 SitesService sitesService) {
    CatalogLinkValidator validator = new CatalogLinkValidator(commerceConnectionInitializer, sitesService, EXTERNALID_PROPERTY);
    validator.setConnection(connection);
    validator.setValidatingSubtypes(true);
    return validator;
  }

  @SuppressWarnings("MethodMayBeStatic")
  @Bean
  CatalogLinkValidator productTeaserExternalIdValidator(CapConnection connection,
                                                        CommerceConnectionInitializer commerceConnectionInitializer,
                                                        SitesService sitesService) {
    CatalogLinkValidator validator = createCatalogLinkValidator(connection, commerceConnectionInitializer, sitesService);
    validator.setContentType("CMProductTeaser");
    return validator;
  }

  @SuppressWarnings("MethodMayBeStatic")
  @Bean
  CatalogLinkValidator marketingSpotExternalIdValidator(CapConnection connection,
                                                        CommerceConnectionInitializer commerceConnectionInitializer,
                                                        SitesService sitesService) {
    CatalogLinkValidator validator = createCatalogLinkValidator(connection, commerceConnectionInitializer, sitesService);
    validator.setContentType("CMMarketingSpot");
    return validator;
  }

  @SuppressWarnings("MethodMayBeStatic")
  @Bean
  CatalogLinkValidator productListExternalIdValidator(CapConnection connection,
                                                      CommerceConnectionInitializer commerceConnectionInitializer,
                                                      SitesService sitesService) {
    CatalogLinkValidator validator = createCatalogLinkValidator(connection, commerceConnectionInitializer, sitesService);
    validator.setContentType(CMPRODUCTLIST_NAME);
    validator.setOptional(true);
    return validator;
  }

  @SuppressWarnings("MethodMayBeStatic")
  @Bean
  CatalogLinkValidator externalChannelExternalIdValidator(CapConnection connection,
                                                          CommerceConnectionInitializer commerceConnectionInitializer,
                                                          SitesService sitesService) {
    CatalogLinkValidator validator = new ExternalChannelValidator(commerceConnectionInitializer, sitesService, EXTERNALID_PROPERTY);
    validator.setConnection(connection);
    validator.setContentType(CMEXTERNALCHANNEL_NAME);
    validator.setValidatingSubtypes(true);
    return validator;
  }

  @SuppressWarnings("MethodMayBeStatic")
  @Bean
  CatalogLinkValidator externalProductExternalIdValidator(CapConnection connection,
                                                          CommerceConnectionInitializer commerceConnectionInitializer,
                                                          SitesService sitesService) {
    CatalogLinkValidator validator = new ExternalProductValidator(commerceConnectionInitializer, sitesService, EXTERNALID_PROPERTY);
    validator.setConnection(connection);
    validator.setContentType("CMExternalProduct");
    validator.setValidatingSubtypes(true);
    return validator;
  }

  @SuppressWarnings("MethodMayBeStatic")
  @Bean
  CatalogLinkValidator externalPageExternalIdValidator(CapConnection connection,
                                                       CommerceConnectionInitializer commerceConnectionInitializer,
                                                       SitesService sitesService) {
    CatalogLinkValidator validator = new ExternalPageValidator(commerceConnectionInitializer, sitesService, EXTERNALID_PROPERTY);
    validator.setConnection(connection);
    validator.setContentType("CMExternalPage");
    validator.setValidatingSubtypes(true);
    return validator;
  }

  @SuppressWarnings("MethodMayBeStatic")
  @Bean
  IsLiveContextTypeValidator productListSupportedValidator(CapConnection connection,
                                                           CommerceConnectionInitializer commerceConnectionInitializer,
                                                           SitesService sitesService) {
    IsLiveContextTypeValidator validator = new IsLiveContextTypeValidator(commerceConnectionInitializer, sitesService);
    validator.setConnection(connection);
    validator.setContentType(CMPRODUCTLIST_NAME);
    validator.setValidatingSubtypes(true);
    return validator;
  }

  @SuppressWarnings("MethodMayBeStatic")
  @Bean
  StructLinkListIndexValidator productListIndexValidator(CapConnection connection) {
    StructLinkListIndexValidator validator = new StructLinkListIndexValidator();
    validator.setConnection(connection);
    validator.setContentType(CMPRODUCTLIST_NAME);
    validator.setListPropertyName("links");
    validator.setMaxLengthPropertyName("maxLength");
    validator.setPropertyName("extendedItems");
    return validator;
  }

  @SuppressWarnings("MethodMayBeStatic")
  @Bean
  ExternalPagePartOfNavigationValidator externalPagePartOfNavigationValidator(CapConnection connection,
                                                                              SitesService sitesService) {
    ExternalPagePartOfNavigationValidator validator = new ExternalPagePartOfNavigationValidator(sitesService);
    validator.setConnection(connection);
    validator.setContentType("CMExternalPage");
    return validator;
  }

  @SuppressWarnings("MethodMayBeStatic")
  @Bean
  ChannelSegmentValidator cmExternalChannelSegmentValidator(CapConnection connection,
                                                            UrlPathFormattingHelper urlPathFormattingHelper) {
    ChannelSegmentValidator validator = new ChannelSegmentValidator(urlPathFormattingHelper);
    validator.setConnection(connection);
    validator.setContentType(CMEXTERNALCHANNEL_NAME);
    return validator;
  }

  @SuppressWarnings("MethodMayBeStatic")
  @Bean
  SegmentFormatValidator segmentFormatValidator(CapConnection connection,
                                                CommerceConnectionInitializer commerceConnectionInitializer,
                                                SitesService sitesService) {
    SegmentFormatValidator validator = new SegmentFormatValidator(commerceConnectionInitializer, sitesService, "segment");
    validator.setConnection(connection);
    validator.setContentType("CMChannel");
    validator.setFallbackPropertyName("title");
    validator.setValidatingSubtypes(true);
    return validator;
  }

  @SuppressWarnings("MethodMayBeStatic")
  @Bean
  ChannelNavigationValidator cmExternalChannelNavigationValidator(CapConnection connection) {
    ChannelNavigationValidator validator = new ChannelNavigationValidator();
    validator.setConnection(connection);
    validator.setContentType(CMEXTERNALCHANNEL_NAME);
    return validator;
  }

  @SuppressWarnings("MethodMayBeStatic")
  @Bean
  ChannelReferrerValidator cmExternalChannelReferrerValidator(CapConnection connection) {
    ChannelReferrerValidator validator = new ChannelReferrerValidator();
    validator.setConnection(connection);
    validator.setContentType(CMEXTERNALCHANNEL_NAME);
    return validator;
  }
}
