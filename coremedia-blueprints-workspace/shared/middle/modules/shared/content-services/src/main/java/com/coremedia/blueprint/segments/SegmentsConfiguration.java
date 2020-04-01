package com.coremedia.blueprint.segments;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SegmentsConfiguration {

  @Bean
  CMLinkableSegmentStrategy cmlinkableSegmentStrategy() {
    return new CMLinkableSegmentStrategy();
  }

  @Bean
  CMTaxonomySegmentStrategy cmtaxonomySegmentStrategy() {
    return new CMTaxonomySegmentStrategy();
  }

  @Bean
  CMPersonSegmentStrategy cmpersonSegmentStrategy() {
    return new CMPersonSegmentStrategy();
  }
}
