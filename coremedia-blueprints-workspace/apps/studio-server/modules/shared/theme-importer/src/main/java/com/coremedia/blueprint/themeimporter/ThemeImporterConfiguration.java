package com.coremedia.blueprint.themeimporter;

import com.coremedia.blueprint.localization.LocalizationService;
import com.coremedia.blueprint.localization.configuration.LocalizationServiceConfiguration;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.struct.StructService;
import com.coremedia.cap.themeimporter.ThemeImporter;
import com.coremedia.mimetype.MimeTypeService;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Configuration
@Import(LocalizationServiceConfiguration.class)
@ImportResource(
        value = {
                "classpath:/com/coremedia/mimetype/mimetype-service.xml"
        },
        reader = ResourceAwareXmlBeanDefinitionReader.class)
public class ThemeImporterConfiguration {

  @Bean
  public ThemeImporter themeImporter(CapConnection capConnection,
                                     MimeTypeService mimeTypeService,
                                     LocalizationService localizationService,
                                     MapToStructAdapter mapToStructAdapter,
                                     SettingsJsonToMapAdapter settingsJsonToMapAdapter) {
    return new ThemeImporterImpl(capConnection, mimeTypeService, localizationService, mapToStructAdapter, settingsJsonToMapAdapter);
  }

  @Bean
  public MapToStructAdapter settingsJsonToStructAdapter(StructService structService,
                                                        ContentRepository contentRepository) {
    return new MapToStructAdapter(structService, contentRepository);
  }

  @Bean
  public SettingsJsonToMapAdapter settingsJsonToMapAdapter(ContentRepository contentRepository) {
    return new SettingsJsonToMapAdapter(contentRepository);
  }
}
