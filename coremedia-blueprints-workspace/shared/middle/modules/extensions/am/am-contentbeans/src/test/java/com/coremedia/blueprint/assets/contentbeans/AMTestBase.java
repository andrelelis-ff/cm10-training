package com.coremedia.blueprint.assets.contentbeans;


import com.coremedia.blueprint.testing.ContentTestHelper;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@ContextConfiguration(classes = AMTaxonomyImplTest.LocalConfig.class)
@ActiveProfiles(AMTaxonomyImplTest.LocalConfig.PROFILE)
public class AMTestBase {

  private static final String CONTENT_REPOSITORY_URL = "classpath:/com/coremedia/blueprint/assets/contentbeans/am-test-content.xml";

  @Inject
  private ContentTestHelper contentTestHelper;

  <T> T getContentBean(int id) {
    return contentTestHelper.getContentBean(id);
  }

  @Configuration
  @Import(XmlRepoConfiguration.class)
  @ImportResource(value = {
          "classpath:/com/coremedia/cap/common/xml/uapi-xml-services.xml",
          "classpath:/com/coremedia/blueprint/base/settings/impl/bpbase-settings-services.xml",
          "classpath:/framework/spring/blueprint-contentbeans.xml",
          "classpath:/framework/spring/blueprint-contentbeans-settings.xml",
          "classpath:/framework/spring/am-contentbeans.xml"
  },
          reader = ResourceAwareXmlBeanDefinitionReader.class)
  @Profile(LocalConfig.PROFILE)
  static class LocalConfig {
    public static final String PROFILE = "AMTests";

    @Bean
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig(CONTENT_REPOSITORY_URL);
    }

    @Bean
    public ContentTestHelper contentTestHelper() {
      return new ContentTestHelper();
    }
  }
}
