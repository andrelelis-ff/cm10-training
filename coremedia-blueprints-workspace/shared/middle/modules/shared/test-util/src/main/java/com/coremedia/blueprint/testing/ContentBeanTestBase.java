package com.coremedia.blueprint.testing;

import com.coremedia.cache.Cache;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.CACHE;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.CONTENT_BEAN_FACTORY;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.DATA_VIEW_FACTORY;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.ID_PROVIDER;
import static com.coremedia.cap.test.xmlrepo.XmlRepoResources.LINK_FORMATTER;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;


/**
 * Base test infrastructure all content bean tests
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = ContentBeanTestBase.LocalConfig.class)
@ActiveProfiles(ContentBeanTestBase.LocalConfig.PROFILE)
public abstract class ContentBeanTestBase {
  @Configuration
  @ImportResource(
          value = {
                  CACHE,
                  CONTENT_BEAN_FACTORY,
                  DATA_VIEW_FACTORY,
                  ID_PROVIDER,
                  LINK_FORMATTER,
          },
          reader = ResourceAwareXmlBeanDefinitionReader.class
  )
  @Import({XmlRepoConfiguration.class, ContentTestConfiguration.class})
  @Profile(LocalConfig.PROFILE)
  public static class LocalConfig {
    public static final String PROFILE = "ContentBeanTestBase";
    private static final String CONTENT_REPOSITORY = "classpath:/com/coremedia/testing/contenttest.xml";

    @Bean
    @Scope(SCOPE_SINGLETON)
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig(CONTENT_REPOSITORY);
    }
  }

  @Inject
  private MockHttpServletRequest request;
  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private MockHttpServletResponse response;
  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private ContentTestHelper contentTestHelper;
  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private ContentBeanFactory contentBeanFactory;
  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private DataViewFactory dataViewFactory;
  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private ContentRepository contentRepository;
  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private Cache cache;

  public Content getContent(int id) {
    return contentTestHelper.getContent(id);
  }

  /**
   * Returns the ContentBean with the given id.
   *
   * @param id Id of ContentBean to get
   * @return ContentBean
   */
  protected <T> T getContentBean(int id) {
    return contentTestHelper.getContentBean(id);
  }

  public ContentBeanFactory getContentBeanFactory() {
    return contentBeanFactory;
  }

  public DataViewFactory getDataViewFactory() {
    return dataViewFactory;
  }

  public ContentRepository getContentRepository() {
    return contentRepository;
  }

  public Cache getCache() {
    return cache;
  }

  public void setUpPreviewDate(String requestAttributePreviewDateName) {
    setUpPreviewDate(requestAttributePreviewDateName, 2005, Calendar.JANUARY, 1);
  }

  public void setUpPreviewDate(String requestAttributePreviewDateName, int year, int month, int day) {
    setUpPreviewDate(this.request, requestAttributePreviewDateName, year, month, day);
  }

  public static void setUpPreviewDate(MockHttpServletRequest mockRequest, String requestAttributePreviewDateName, int year, int month, int day) {
    Calendar now = GregorianCalendar.getInstance();
    now.set(Calendar.YEAR, year);
    now.set(Calendar.MONTH, month);
    now.set(Calendar.DAY_OF_MONTH, day);

    RequestAttributes requestAttributes = new ServletRequestAttributes(mockRequest);
    RequestContextHolder.setRequestAttributes(requestAttributes);

    setRequestAttribute(mockRequest, now, requestAttributePreviewDateName, ServletRequestAttributes.SCOPE_REQUEST);
  }

  public void setRequestAttribute(Object value, String attributeName, int scope) {
    setRequestAttribute(this.request, value, attributeName, scope);
  }

  public static void setRequestAttribute(MockHttpServletRequest request, Object value, String attributeName, int scope) {
    RequestAttributes requestAttributes = new ServletRequestAttributes(request);
    requestAttributes.setAttribute(attributeName, value, scope);
    RequestContextHolder.setRequestAttributes(requestAttributes);
  }

  @After
  public void teardown() {
    // make sure that tests do not interfere with each other via thread locals!
    RequestContextHolder.resetRequestAttributes();
  }
}
