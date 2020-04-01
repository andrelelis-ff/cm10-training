package com.coremedia.lc.studio.lib;

import com.coremedia.blueprint.base.rest.validators.UniqueInSiteStringValidator;
import com.coremedia.cap.util.ContentStringPropertyIndex;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.augmentation.config.LcAugmentationLegacyAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Set;
import java.util.function.Function;

import static com.coremedia.livecontext.augmentation.config.LcAugmentationLegacyAutoConfiguration.CM_EXTERNAL_CHANNEL;
import static com.coremedia.livecontext.augmentation.config.LcAugmentationLegacyAutoConfiguration.CM_EXTERNAL_PAGE;
import static com.coremedia.livecontext.augmentation.config.LcAugmentationLegacyAutoConfiguration.EXTERNAL_ID;

@Configuration
@ConditionalOnProperty(
        name = "livecontext.augmentation.backward-compatibility",
        havingValue = "true"
)
@Import(LcAugmentationLegacyAutoConfiguration.class)
public class LcStudioValidationLegacyConfiguration {

  @Bean
  UniqueInSiteStringValidator externalPageUniqueExternalIdValidator(ContentRepository contentRepository,
                                                                    ContentStringPropertyIndex catalogExternalPageIndex,
                                                                    SitesService sitesService) {
    Function<String, Set<Content>> lookupFunction = catalogExternalPageIndex::getContentsWithValue;
    return new UniqueInSiteStringValidator(contentRepository, CM_EXTERNAL_PAGE, EXTERNAL_ID, lookupFunction, sitesService);
  }

  @Bean
  UniqueInSiteStringValidator externalChannelUniqueExternalIdValidator(ContentRepository contentRepository,
                                                                       ContentStringPropertyIndex catalogExternalChannelIndex,
                                                                       SitesService sitesService) {
    Function<String, Set<Content>> lookupFunction = catalogExternalChannelIndex::getContentsWithValue;
    return new UniqueInSiteStringValidator(contentRepository, CM_EXTERNAL_CHANNEL, EXTERNAL_ID, lookupFunction, sitesService);
  }
}
