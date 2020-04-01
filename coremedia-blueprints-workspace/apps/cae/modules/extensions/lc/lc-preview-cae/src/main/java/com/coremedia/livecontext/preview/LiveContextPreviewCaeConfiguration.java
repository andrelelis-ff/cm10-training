package com.coremedia.livecontext.preview;

import com.coremedia.springframework.boot.web.servlet.RegistrationBeanBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiveContextPreviewCaeConfiguration {
  @Bean
  FilterRegistrationBean previewTokenMarkerFilter(){
    return RegistrationBeanBuilder.forFilter(new PreviewMarkerFilter())
            .order(20_000)
            .build();
  }
}
