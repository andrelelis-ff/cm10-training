package com.coremedia.livecontext.contentbeans;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextBuilderImpl;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.cache.Cache;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.livecontext.augmentation.config.LcAugmentationLegacyAutoConfiguration;
import com.coremedia.livecontext.context.LiveContextNavigation;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.navigation.LiveContextNavigationFactory;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.blueprint.common.datevalidation.ValidityPeriodValidator.REQUEST_ATTRIBUTE_PREVIEW_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        XmlRepoConfiguration.class,
        LiveContextExternalChannelImplLegacyTest.LocalConfig.class
})
@TestPropertySource(properties = {
        "livecontext.cache.invalidation.enabled=false",
        "livecontext.augmentation.backward-compatibility=true"
})
public class LiveContextExternalChannelImplLegacyTest {

  @Configuration
  @ImportResource({
          "classpath:/framework/spring/blueprint-contentbeans.xml",
          "classpath:META-INF/coremedia/livecontext-contentbeans.xml",
          "classpath:META-INF/coremedia/livecontext-contentbeans-settings.xml",
  })
  @ComponentScan("com.coremedia.blueprint.base.livecontext.augmentation")
  @Import(LcAugmentationLegacyAutoConfiguration.class)
  static class LocalConfig {

    @Bean
    StoreContext storeContext() {
      CommerceConnection connection = mock(CommerceConnection.class);
      return StoreContextBuilderImpl.from(connection, "any-site-id").build();
    }

    @Bean
    XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig("classpath:/com/coremedia/livecontext/contentbeans/contenttest.xml");
    }
  }

  private LiveContextExternalChannelImpl testling;

  @Inject
  private SettingsService settingsService;

  @Inject
  private SitesService sitesService;

  @Mock
  private LiveContextNavigationFactory liveContextNavigationFactory;

  @Mock
  private Category category;

  @Mock
  private StoreContext storeContext;

  @Mock
  private CommerceConnectionInitializer commerceConnectionInitializer;

  @Inject
  private ContentRepository contentRepository;

  @Inject
  private ContentBeanFactory contentBeanFactory;

  @Inject
  private DataViewFactory dataViewFactory;

  @Inject
  private Cache cache;

  public static void setUpPreviewDate() {
    setUpPreviewDate(2005, Calendar.JANUARY, 1);
  }

  public static void setUpPreviewDate(int year, int month, int day) {
    Calendar now = GregorianCalendar.getInstance();
    now.set(Calendar.YEAR, year);
    now.set(Calendar.MONTH, month);
    now.set(Calendar.DAY_OF_MONTH, day);
    setRequestAttribute(now, REQUEST_ATTRIBUTE_PREVIEW_DATE, ServletRequestAttributes.SCOPE_REQUEST);
  }

  public static void setRequestAttribute(Object value, String attributeName, int scope) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestAttributes requestAttributes = new ServletRequestAttributes(request);
    requestAttributes.setAttribute(attributeName, value, scope);
    RequestContextHolder.setRequestAttributes(requestAttributes);
  }

  @Before
  public void before() {
    testling = getContentBean(100);
    setUpPreviewDate();

    initMocks(this);

    CatalogService catalogService = mock(CatalogService.class);
    when(catalogService.findCategoryById(any(), any(StoreContext.class))).thenReturn(category);

    CommerceConnection commerceConnection = mock(CommerceConnection.class);
    when(commerceConnection.getStoreContext()).thenReturn(storeContext);
    when(commerceConnection.getCatalogService()).thenReturn(catalogService);

    when(commerceConnectionInitializer.findConnectionForSite(any(Site.class)))
            .thenReturn(Optional.of(commerceConnection));

    when(liveContextNavigationFactory.createNavigation(any(Category.class), any(Site.class)))
            .thenReturn(mock(LiveContextNavigation.class));

    testling.setLiveContextNavigationFactory(liveContextNavigationFactory);
    testling.setCommerceConnectionInitializer(commerceConnectionInitializer);
  }

  @After
  public void teardown() {
    // make sure that tests do not interfere with each other via thread locals!
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  public void testSettingsMechanism() throws Exception {
    Map setting = settingsService.setting(LiveContextExternalChannelImpl.COMMERCE_STRUCT, Map.class, testling);
    assertThat(setting).hasSize(3);
  }

  @Test
  public void testGetExternalChildrenWithSelectedCategories() throws Exception {
    assertThat(testling.isCommerceChildrenSelected()).isTrue();
    Site site = sitesService.getContentSiteAspect(testling.getContent()).getSite();

    List<Linkable> externalChildren = testling.getExternalChildren(site);
    assertThat(externalChildren).hasSize(3);
  }

  public Content getContent(int id) {
    return contentRepository.getContent(IdHelper.formatContentId(id));
  }

  /**
   * Returns the ContentBean with the given id.
   *
   * @param id Id of ContentBean to get
   * @return ContentBean
   */
  protected <T> T getContentBean(int id) {
    return (T) contentBeanFactory.createBeanFor(getContent(id), ContentBean.class);
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
}
