package com.coremedia.livecontext.augmentation.config;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AugmentationServiceImpl;
import com.coremedia.cap.util.CacheableContentStringPropertyIndex;
import com.coremedia.cap.util.ContentStringPropertyIndex;
import com.coremedia.cache.Cache;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ConditionalOnProperty(
        name = "livecontext.augmentation.backward-compatibility",
        havingValue = "true"
)
@ImportResource(value = {
        "classpath:/com/coremedia/cap/multisite/multisite-services.xml"
}, reader = ResourceAwareXmlBeanDefinitionReader.class)
public class LcAugmentationLegacyAutoConfiguration {

  public static final String CM_EXTERNAL_CHANNEL = "CMExternalChannel";
  public static final String CM_EXTERNAL_PAGE = "CMExternalPage";
  private static final String CM_EXTERNAL_PRODUCT = "CMExternalProduct";
  public static final String EXTERNAL_ID = "externalId";

  @Bean
  AugmentationService categoryAugmentationService(SitesService sitesService,
                                                  ContentStringPropertyIndex catalogExternalChannelIndex) {
    AugmentationServiceImpl augmentationService = new AugmentationServiceImpl();
    augmentationService.setSitesService(sitesService);
    augmentationService.setIndex(catalogExternalChannelIndex);
    return augmentationService;
  }

  @Bean
  CacheableContentStringPropertyIndex catalogExternalChannelIndex(ContentRepository contentRepository, Cache cache) {
    return new CacheableContentStringPropertyIndex(contentRepository, CM_EXTERNAL_CHANNEL, EXTERNAL_ID, cache);
  }

  @Bean
  AugmentationService externalPageAugmentationService(SitesService sitesService,
                                                      ContentStringPropertyIndex catalogExternalPageIndex) {
    AugmentationServiceImpl augmentationService = new AugmentationServiceImpl();
    augmentationService.setSitesService(sitesService);
    augmentationService.setIndex(catalogExternalPageIndex);
    return augmentationService;
  }

  @Bean
  ContentStringPropertyIndex catalogExternalPageIndex(ContentRepository contentRepository) {
    return new ContentStringPropertyIndex(contentRepository, CM_EXTERNAL_PAGE, EXTERNAL_ID);
  }

  @Bean
  AugmentationService productAugmentationService(SitesService sitesService,
                                                 ContentStringPropertyIndex externalProductIndex) {
    AugmentationServiceImpl augmentationService = new AugmentationServiceImpl();
    augmentationService.setSitesService(sitesService);
    augmentationService.setIndex(externalProductIndex);
    return augmentationService;
  }

  @Bean
  CacheableContentStringPropertyIndex externalProductIndex(ContentRepository contentRepository, Cache cache) {
    return new CacheableContentStringPropertyIndex(contentRepository, CM_EXTERNAL_PRODUCT, EXTERNAL_ID, cache);
  }

}
