package com.coremedia.lc.studio.lib;

import com.coremedia.blueprint.base.rest.validators.UniqueInSiteStringValidator;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.observe.ObservedPropertyService;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import java.util.Set;
import java.util.function.Function;

import static com.coremedia.livecontext.augmentation.config.LcAugmentationLegacyAutoConfiguration.CM_EXTERNAL_CHANNEL;
import static com.coremedia.livecontext.augmentation.config.LcAugmentationLegacyAutoConfiguration.CM_EXTERNAL_PAGE;
import static com.coremedia.livecontext.augmentation.config.LcAugmentationLegacyAutoConfiguration.EXTERNAL_ID;

@Configuration
@ConditionalOnProperty(
        name = "livecontext.augmentation.backward-compatibility",
        havingValue = "false",
        matchIfMissing = true
)
@ImportResource(value = {
        "classpath:/com/coremedia/cap/multisite/multisite-services.xml"
}, reader = ResourceAwareXmlBeanDefinitionReader.class)
public class LcStudioValidationConfiguration {

  @Bean
  UniqueInSiteStringValidator externalPageUniqueExternalIdValidator(ContentRepository contentRepository,
                                                                    SitesService sitesService) {
    CapPropertyDescriptor capPropertyDescriptor = lookupDescriptor(contentRepository, CM_EXTERNAL_PAGE);
    ObservedPropertyService observedPropertyService = contentRepository.getObservedPropertyService();
    Function<String, Set<Content>> lookupFunction =
            (String value) -> observedPropertyService.getContentsWithValue(value, capPropertyDescriptor);
    return new UniqueInSiteStringValidator(contentRepository, CM_EXTERNAL_PAGE, EXTERNAL_ID, lookupFunction, sitesService);
  }

  @Bean
  UniqueInSiteStringValidator externalChannelUniqueExternalIdValidator(ContentRepository contentRepository,
                                                                       SitesService sitesService) {
    CapPropertyDescriptor capPropertyDescriptor = lookupDescriptor(contentRepository, CM_EXTERNAL_CHANNEL);
    ObservedPropertyService observedPropertyService = contentRepository.getObservedPropertyService();
    Function<String, Set<Content>> lookupFunction =
            (String value) -> observedPropertyService.getContentsWithValue(value, capPropertyDescriptor);
    return new UniqueInSiteStringValidator(contentRepository, CM_EXTERNAL_CHANNEL, EXTERNAL_ID, lookupFunction, sitesService);
  }

  private static CapPropertyDescriptor lookupDescriptor(ContentRepository contentRepository, String documentTypeName) {
    ContentType contentType = contentRepository.getContentType(documentTypeName);
    if (contentType == null) {
      throw new IllegalStateException("Required content type " + documentTypeName + " not found.");
    }
    CapPropertyDescriptor descriptor = contentType.getDescriptor(EXTERNAL_ID);
    if (descriptor == null) {
      throw new IllegalStateException("Required property decriptor " + EXTERNAL_ID + " not found on type " + documentTypeName + ".");
    }
    return descriptor;
  }
}
