package com.coremedia.blueprint.cae.view.resolver;

import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.testing.ContentTestConfiguration;
import com.coremedia.blueprint.testing.ContentTestHelper;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.cae.view.resolver.BlueprintViewRepositoryNameProviderTest.LocalConfig.PROFILE;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.CACHE;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.CONTENT_BEAN_FACTORY;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.DATA_VIEW_FACTORY;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.ID_PROVIDER;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.LINK_FORMATTER;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = BlueprintViewRepositoryNameProviderTest.LocalConfig.class)
@ActiveProfiles(PROFILE)
public class BlueprintViewRepositoryNameProviderTest {
  @Configuration
  @ImportResource(
          value = {
                  CONTENT_BEAN_FACTORY,
                  DATA_VIEW_FACTORY,
                  ID_PROVIDER,
                  LINK_FORMATTER,
                  CACHE,
                  "classpath:/framework/spring/blueprint-contentbeans.xml",
                  "classpath:/framework/spring-test/blueprint-view-repository-name-provider-test.xml",
          },
          reader = ResourceAwareXmlBeanDefinitionReader.class
  )
  @Import({XmlRepoConfiguration.class, ContentTestConfiguration.class})
  @Profile(PROFILE)
  public static class LocalConfig {
    public static final String PROFILE = "BlueprintViewRepositoryNameProviderTest";
    private static final String CONTENT_REPOSITORY = "classpath:/com/coremedia/testing/contenttest.xml";

    @Bean
    @Scope(SCOPE_SINGLETON)
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig(CONTENT_REPOSITORY);
    }
  }

  @Inject
  private BlueprintViewRepositoryNameProvider blueprintViewRepositoryNameProvider;

  @Inject
  private ContentTestHelper contentTestHelper;

  @Inject
  private MockHttpServletRequest request;

  private CMChannel channel;

  @Before
  public void setUp() {
    channel = contentTestHelper.getContentBean(10);
  }

  @Test
  public void testGetViewRepositoryNames() {
    request.setAttribute("com.coremedia.blueprint.viewrepositorynames", null);
    Map<String, CMLinkable> map = new HashMap<>();
    map.put(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION, channel);
    List<String> repositoryNames = blueprintViewRepositoryNameProvider.getViewRepositoryNames("view", map, null, request);
    Assert.assertEquals("media", repositoryNames.get(0));
    Assert.assertEquals("notMedia", repositoryNames.get(1));
    Assert.assertEquals("againNotMedia", repositoryNames.get(2));
    Assert.assertEquals("basic", repositoryNames.get(3));
    Assert.assertEquals("error", repositoryNames.get(4));
  }
}
