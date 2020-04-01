package com.coremedia.blueprint.sfmc.cae;

import com.coremedia.blueprint.base.sfmc.contentlib.context.SFMCContextConfiguration;
import com.coremedia.blueprint.base.sfmc.contentlib.context.SFMCContextProvider;
import com.coremedia.blueprint.base.sfmc.libservices.dataextensions.SFMCDataExtensionConfiguration;
import com.coremedia.blueprint.base.sfmc.libservices.dataextensions.SFMCDataExtensionService;
import com.coremedia.blueprint.sfmc.cae.dataextension.handler.SFMCFormHandler;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Configuration
@Import({SFMCContextConfiguration.class,
         SFMCDataExtensionConfiguration.class})
@ImportResource("classpath:/com/coremedia/cae/contentbean-services.xml")
public class SFMCCaeConfiguration {
  @Bean
  @NonNull
  SFMCFormHandler sfmcFormHandler(@NonNull SFMCContextProvider sfmcContextProvider,
                                  @NonNull SFMCDataExtensionService sfmcDataExtensionService,
                                  @NonNull ContentBeanFactory contentBeanFactory) {
    return new SFMCFormHandler(sfmcContextProvider, sfmcDataExtensionService, contentBeanFactory);
  }
}
