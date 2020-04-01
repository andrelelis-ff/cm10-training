package com.coremedia.blueprint.studio.rest.differencing;

import com.coremedia.blueprint.base.rest.propertyinferrer.CallToActionConfigurationPropertyInferrer;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.transform.TransformImageService;
import com.coremedia.rest.cap.content.imagevariants.ImageVariantsPropertiesInferrer;
import com.coremedia.rest.cap.differencing.ZeroDefaultValuePropertyInferrer;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource(
        value = {
                "classpath:/com/coremedia/cap/transform/transform-services.xml"
        },
        reader = ResourceAwareXmlBeanDefinitionReader.class)
class InferrersStudioConfiguration {

  @Bean
  CallToActionConfigurationPropertyInferrer callToActionConfigurationPropertyInferrer(@Value("CMTeasable") ContentType contentType) {
    CallToActionConfigurationPropertyInferrer callToActionConfigurationPropertyInferrer = new CallToActionConfigurationPropertyInferrer();
    callToActionConfigurationPropertyInferrer.setType(contentType);
    callToActionConfigurationPropertyInferrer.setApplicableToSubtypes(true);
    return callToActionConfigurationPropertyInferrer;
  }

  @Bean
  ImageVariantsPropertiesInferrer imageVariantsPropertiesInferrer(TransformImageService transformImageService,
                                                                  CapConnection connection,
                                                                  @Value("CMPicture") ContentType contentType) {
    ImageVariantsPropertiesInferrer imageVariantsPropertiesInferrer = new ImageVariantsPropertiesInferrer();
    imageVariantsPropertiesInferrer.setTransformImageService(transformImageService);
    imageVariantsPropertiesInferrer.setConnection(connection);
    imageVariantsPropertiesInferrer.setStructProperty("localSettings");
    imageVariantsPropertiesInferrer.setDataProperty("data");
    imageVariantsPropertiesInferrer.setType(contentType);
    imageVariantsPropertiesInferrer.setApplicableToSubtypes(true);
    return imageVariantsPropertiesInferrer;
  }

  @Bean
  ZeroDefaultValuePropertyInferrer notSearchablePropertiesInferrer(@Value("CMTeasable") ContentType contentType) {
    ZeroDefaultValuePropertyInferrer notSearchablePropertiesInferrer = new ZeroDefaultValuePropertyInferrer();
    notSearchablePropertiesInferrer.setType(contentType);
    notSearchablePropertiesInferrer.setApplicableToSubtypes(true);
    notSearchablePropertiesInferrer.setProperty("notSearchable");
    return notSearchablePropertiesInferrer;
  }

  @Bean
  ZeroDefaultValuePropertyInferrer hiddenPropertiesInferrer(@Value("CMNavigation") ContentType contentType) {
    ZeroDefaultValuePropertyInferrer hiddenPropertiesInferrer = new ZeroDefaultValuePropertyInferrer();
    hiddenPropertiesInferrer.setType(contentType);
    hiddenPropertiesInferrer.setApplicableToSubtypes(true);
    hiddenPropertiesInferrer.setProperty("hidden");
    return hiddenPropertiesInferrer;
  }

  @Bean
  ZeroDefaultValuePropertyInferrer hiddenInSitemapPropertiesInferrer(@Value("CMNavigation") ContentType contentType) {
    ZeroDefaultValuePropertyInferrer hiddenInSitemapPropertiesInferrer = new ZeroDefaultValuePropertyInferrer();
    hiddenInSitemapPropertiesInferrer.setType(contentType);
    hiddenInSitemapPropertiesInferrer.setApplicableToSubtypes(true);
    hiddenInSitemapPropertiesInferrer.setProperty("hiddenInSitemap");
    return hiddenInSitemapPropertiesInferrer;
  }

  @Bean
  ZeroDefaultValuePropertyInferrer inHeadPropertiesInferrer(@Value("CMJavaScript") ContentType contentType) {
    ZeroDefaultValuePropertyInferrer inHeadPropertiesInferrer = new ZeroDefaultValuePropertyInferrer();
    inHeadPropertiesInferrer.setType(contentType);
    inHeadPropertiesInferrer.setApplicableToSubtypes(true);
    inHeadPropertiesInferrer.setProperty("inHead");
    return inHeadPropertiesInferrer;
  }

  @Bean
  ZeroDefaultValuePropertyInferrer ieReleavedPropertiesInferrer(@Value("CMAbstractCode") ContentType contentType) {
    ZeroDefaultValuePropertyInferrer ieReleavedPropertiesInferrer = new ZeroDefaultValuePropertyInferrer();
    ieReleavedPropertiesInferrer.setType(contentType);
    ieReleavedPropertiesInferrer.setApplicableToSubtypes(true);
    ieReleavedPropertiesInferrer.setProperty("ieRevealed");
    return ieReleavedPropertiesInferrer;
  }

  /**
   * @deprecated We will be removing the compression of code from the CAE as the frontend workspace provides options to compress
   *             the code before it is uploaded to the content repository. When the compression is removed this property does not
   *             have any effect.
   */
  @Bean
  @Deprecated
  ZeroDefaultValuePropertyInferrer disableCompressPropertiesInferrer(@Value("CMAbstractCode") ContentType contentType) {
    ZeroDefaultValuePropertyInferrer disableCompressPropertiesInferrer = new ZeroDefaultValuePropertyInferrer();
    disableCompressPropertiesInferrer.setType(contentType);
    disableCompressPropertiesInferrer.setApplicableToSubtypes(true);
    disableCompressPropertiesInferrer.setProperty("disableCompress");
    return disableCompressPropertiesInferrer;
  }
}
