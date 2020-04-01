package com.coremedia.blueprint.feeder.populate;

import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.feeder.MutableFeedable;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static com.coremedia.blueprint.feeder.populate.StructFeedablePopulatorTest.LocalConfig.PROFILE;
import static org.junit.Assert.assertEquals;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = StructFeedablePopulatorTest.LocalConfig.class)
@ActiveProfiles(PROFILE)
public class StructFeedablePopulatorTest {
  @Configuration
  @Import(XmlRepoConfiguration.class)
  @Profile(PROFILE)
  public static class LocalConfig {
    public static final String PROFILE = "StructFeedablePopulatorTest";
    private static final String CONTENT_REPOSITORY = "classpath:/com/coremedia/testing/contenttest.xml";

    @Bean
    @Scope(SCOPE_SINGLETON)
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig(CONTENT_REPOSITORY);
    }
  }

  private StructFeedablePopulator structFeedablePopulator;
  private Content content;

  @Inject
  private ContentRepository contentRepository;

  @Before
  public void setUp() throws Exception {

    structFeedablePopulator = new StructFeedablePopulator();
    structFeedablePopulator.setSolrFieldName("textbody");
    List<String> propertyNames = Arrays.asList("settings", "localSettings");
    structFeedablePopulator.setPropertyNames(propertyNames);
    content = contentRepository.getContent(IdHelper.formatContentId(4));
  }

  @Test
  public void testPopulate() throws Exception {

    MutableFeedable mutableFeedable = new MutableFeedableImpl() {
      @Override
      public void setStringElement(String s, String s1) {
        assertEquals("unexpected field", "textbody", s);
        assertEquals("unexpected struct", "booleanProperty stringProperty testString integerProperty dateProperty 2010-01-01T10:00:23-10:00 doubleProperty 2.3 linkProperty structProperty ", s1);
      }
    };
    structFeedablePopulator.populate(mutableFeedable, content);
  }
}
