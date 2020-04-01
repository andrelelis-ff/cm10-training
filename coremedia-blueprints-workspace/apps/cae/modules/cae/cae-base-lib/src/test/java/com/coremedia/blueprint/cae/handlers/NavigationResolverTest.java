package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static com.coremedia.blueprint.cae.handlers.NavigationResolverTest.LocalConfig.PROFILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = NavigationResolverTest.LocalConfig.class)
@ActiveProfiles(PROFILE)
public class NavigationResolverTest {
  @Configuration
  @ImportResource(
          value = {
                  "classpath:/framework/spring/blueprint-contentbeans.xml",
                  "classpath:/framework/spring/blueprint-handlers.xml",
          },
          reader = ResourceAwareXmlBeanDefinitionReader.class
  )
  @Import(XmlRepoConfiguration.class)
  @Profile(PROFILE)
  public static class LocalConfig {
    public static final String PROFILE = "NavigationResolverTest";
    private static final String CONTENT_REPOSITORY = "classpath:/com/coremedia/blueprint/cae/handlers/navigationresolver-test-content.xml";

    @Bean
    @Scope(SCOPE_SINGLETON)
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig(CONTENT_REPOSITORY);
    }
  }

  @Inject
  private NavigationResolver testling;

  @Inject
  private ContentBeanFactory contentBeanFactory;

  @Inject
  private ContentRepository contentRepository;

  @Test
  public void resolveContentInRoot() {
    Content content = contentRepository.getContent(IdHelper.formatContentId(4));
    CMLinkable contentBean = contentBeanFactory.createBeanFor(content, CMLinkable.class);
    List<String> navigationPath = Arrays.asList("root");
    Navigation navigation = testling.getNavigation(contentBean, navigationPath);
    checkNavigation(navigation, 124);
  }

  @Test
  public void resolveContentInSubchannel() {
    Content content = contentRepository.getContent(IdHelper.formatContentId(4));
    CMLinkable contentBean = contentBeanFactory.createBeanFor(content, CMLinkable.class);
    List<String> navigationPath = Arrays.asList("root", "level1", "level2");
    Navigation navigation = testling.getNavigation(contentBean, navigationPath);
    checkNavigation(navigation, 130);
  }

  @Test
  public void resolveContentInWrongChannel() {
    Content content = contentRepository.getContent(IdHelper.formatContentId(4));
    CMLinkable contentBean = contentBeanFactory.createBeanFor(content, CMLinkable.class);
    List<String> navigationPath = Arrays.asList("root", "level1");
    Navigation navigation = testling.getNavigation(contentBean, navigationPath);
    assertNull(navigation);
  }

  @Test
  public void resolveContentInNoSuchChannel() {
    Content content = contentRepository.getContent(IdHelper.formatContentId(4));
    CMLinkable contentBean = contentBeanFactory.createBeanFor(content, CMLinkable.class);
    List<String> navigationPath = Arrays.asList("root", "no", "such", "channel");
    Navigation navigation = testling.getNavigation(contentBean, navigationPath);
    assertNull(navigation);
  }

  @Test
  public void resolveRoot() {
    Content content = contentRepository.getContent(IdHelper.formatContentId(124));
    CMLinkable contentBean = contentBeanFactory.createBeanFor(content, CMLinkable.class);
    List<String> navigationPath = Arrays.asList("root");
    Navigation navigation = testling.getNavigation(contentBean, navigationPath);
    checkNavigation(navigation, 124);
  }

  @Test
  public void resolveSubchannel() {
    Content content = contentRepository.getContent(IdHelper.formatContentId(130));
    CMLinkable contentBean = contentBeanFactory.createBeanFor(content, CMLinkable.class);
    List<String> navigationPath = Arrays.asList("root", "level1", "level2");
    Navigation navigation = testling.getNavigation(contentBean, navigationPath);
    checkNavigation(navigation, 130);
  }

  @Test
  public void resolveSubchannelWithoutSelf() {
    Content content = contentRepository.getContent(IdHelper.formatContentId(130));
    CMLinkable contentBean = contentBeanFactory.createBeanFor(content, CMLinkable.class);
    List<String> navigationPath = Arrays.asList("root", "level1");
    Navigation navigation = testling.getNavigation(contentBean, navigationPath);
    assertNull(navigation);
  }


  // --- internal ---------------------------------------------------

  private void checkNavigation(Navigation navigation, int id) {
    assertNotNull(navigation);
    assertTrue(navigation instanceof CMNavigation);
    CMNavigation cmn = (CMNavigation) navigation;
    assertEquals(id, cmn.getContentId());
  }

}
