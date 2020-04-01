package com.coremedia.blueprint.image.transformation;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.coderesources.ThemeService;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.transform.ContentFocusPointResolver;
import com.coremedia.cap.transform.ContentOperationsResolver;
import com.coremedia.cap.transform.VariantsStructResolver;
import com.coremedia.cap.undoc.common.spring.CapRepositoriesConfiguration;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Configuration
@Import(CapRepositoriesConfiguration.class)
@ImportResource(value = {
        "classpath:/com/coremedia/blueprint/base/settings/impl/bpbase-settings-services.xml",
}, reader = ResourceAwareXmlBeanDefinitionReader.class)
public class ImageTransformationConfiguration {

  @Bean
  ContentOperationsResolver cmpictureOperationsResolver() {
    return new CMPictureOperationsResolver();
  }

  @Bean
  ContentFocusPointResolver cmpictureFocusPointResolver() {
    return new CMPictureFocusPointResolver();
  }

  @Bean
  VariantsStructResolver variantsStructResolver(@NonNull SettingsService settingsService,
                                                @NonNull ContentRepository contentRepository,
                                                @NonNull ThemeService themeService) {
    return new VariantsStructResolverImpl(settingsService, contentRepository, themeService);
  }
}
