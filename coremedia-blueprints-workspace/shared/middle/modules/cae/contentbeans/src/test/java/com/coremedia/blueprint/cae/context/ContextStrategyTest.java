package com.coremedia.blueprint.cae.context;

import com.coremedia.blueprint.base.navigation.context.ContextStrategy;
import com.coremedia.blueprint.common.contentbeans.CMArticle;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.testing.ContentTestConfiguration;
import com.coremedia.blueprint.testing.ContentTestHelper;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Locale;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ContextStrategyTest.LocalConfig.class, XmlRepoConfiguration.class})
public class ContextStrategyTest {
  @Inject
  private ContentTestHelper contentTestHelper;

  @Inject
  private ContextStrategy contextStrategy;

  @Test
  public void testDefaultContextStrategyWithLocaleJump() {
    CMArticle germanArticle = contentTestHelper.getContentBean(82);
    CMArticle englishArticle = contentTestHelper.getContentBean(92);
    CMChannel germanChannelOnThirdLevel = contentTestHelper.getContentBean(10030);
    CMChannel englishChannelOnThirdLevel = contentTestHelper.getContentBean(20030);

    // Assume correct Locales
    Assume.assumeTrue("german article should have a german locale", Locale.GERMAN.equals(germanArticle.getLocale()));
    Assume.assumeTrue("english article should have an english locale", Locale.ENGLISH.equals(englishArticle.getLocale()));

    // Assert Master-Derived Relationship
    Assume.assumeTrue("english article should be the language sibling of the german article",
            englishArticle.equals(germanArticle.getVariantsByLocale().get(Locale.ENGLISH)));
    Assume.assumeTrue("english article should have the german article as language master",
            germanArticle.equals(englishArticle.getMaster()));

    // Assert Master-Derived Relationship of Context
    Assume.assumeTrue("englishChannelOnThirdLevel should be the language sibling of the germanChannelOnThirdLevel",
            englishChannelOnThirdLevel.equals(germanChannelOnThirdLevel.getVariantsByLocale().get(Locale.ENGLISH)));
    Assume.assumeTrue("englishChannelOnThirdLevel should have the germanChannelOnThirdLevel as language master",
            germanChannelOnThirdLevel.equals(englishChannelOnThirdLevel.getMaster()));

    // Assert that our expected target is amongst candidates
    Assume.assumeTrue("englishChannelOnThirdLevel should be amongst context candidates of english article",
            englishArticle.getContexts().contains(englishChannelOnThirdLevel));
    Assume.assumeTrue("germanChannelOnThirdLevel should be amongst context candidates of german article",
            germanArticle.getContexts().contains(germanChannelOnThirdLevel));

    // Compute target Context
    Object computedContext = contextStrategy.findAndSelectContextFor(englishArticle, germanChannelOnThirdLevel);

    // Final Assert
    Assert.assertEquals(englishChannelOnThirdLevel, computedContext);
  }

  @Configuration
  @ImportResource(value = {"classpath:/framework/spring/blueprint-contentbeans.xml", "classpath:/framework/spring/blueprint-contextstrategy.xml"},
          reader = ResourceAwareXmlBeanDefinitionReader.class)
  @Import({ContentTestConfiguration.class, XmlRepoConfiguration.class})
  public static class LocalConfig {
    @Bean
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig("classpath:/com/coremedia/blueprint/cae/context/context.xml");
    }
  }
}
