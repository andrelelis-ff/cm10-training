package com.coremedia.livecontext.view;

import com.coremedia.blueprint.cae.contentbeans.CMChannelImpl;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.layout.PageGrid;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {XmlRepoConfiguration.class, PrefetchFragmentsConfigReaderTest.LocalConfig.class, PrefetchFragmentsConfigReader.class})
public class PrefetchFragmentsConfigReaderTest {

  private static final String CONTENT_REPOSITORY_URL = "classpath:/com/coremedia/livecontext/fragment/resolver/fragment-prefetch-test-content.xml";

  @Inject
  private PrefetchFragmentsConfigReader testling;

  @Inject
  private ContentRepository contentRepository;

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private ContentBeanFactory contentBeanFactory;

  @MockBean
  Page page;

  @Before
  public void setUp() {
    when(page.getContext()).thenReturn(loadBeanFor(124, CMChannelImpl.class));
    when(page.getContent()).thenReturn(loadContentFor(124));

    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    mockRequest.setAttribute(ContextHelper.ATTR_NAME_PAGE, page);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
  }

  @Test
  public void getPlacementDefaultView() throws Exception {
    Optional<String> header = testling.getPlacementDefaultView(page, "header");
    assertThat(header).contains("asDefaultFragment");
  }

  @Test
  public void getPlacementViewNotFound() throws Exception {
    Optional<String> notFound = testling.getPlacementView(page, "blub");
    assertThat(notFound).isEmpty();
  }

  @Test
  public void getPredefinedDefaultViews() throws Exception {
    List<String> predefinedViews = testling.getPredefinedDefaultViews(page);
    assertThat(predefinedViews).contains("metadata");
  }

  @Test
  public void getPredefinedViews() throws Exception {
    CMChannel channel = loadBeanFor(124, CMChannel.class);
    List<String> predefinedViews = testling.getPredefinedViews(channel, page);
    assertThat(predefinedViews).contains("channelView");
    assertThat(predefinedViews).doesNotContain("metadata");
  }

  @Test
  public void getPredefinedViewsForArticle() throws Exception {
    Content articleContent = loadContentFor(302);
    List<String> predefinedViewsForContent = testling.getPredefinedViewsForContent(articleContent, page);
    assertThat(predefinedViewsForContent).contains("metadata");
  }

  @Test
  public void getPredefinedViewsForChannel() throws Exception {
    Content channelContent = loadContentFor(124);
    List<String> predefinedViewsForContent = testling.getPredefinedViewsForContent(channelContent, page);
    assertThat(predefinedViewsForContent).contains("channelView");
  }

  @Test
  public void getPredefinedViewsForLayout() throws Exception {
    Content layoutContent = loadContentFor(202);
    PageGrid pg = mock(PageGrid.class);
    when(page.getPageGrid()).thenReturn(pg);
    when(pg.getLayout()).thenReturn(layoutContent);
    List<String> predefinedViewsForContent = testling.getPredefinedViewsForLayout(page);
    assertThat(predefinedViewsForContent).contains("asBreadcrumb");
  }

  @Test
  public void getPredefinedPlacementViewsForLayout() throws Exception {
    Content layoutContent = loadContentFor(202);
    PageGrid pg = mock(PageGrid.class);
    when(page.getPageGrid()).thenReturn(pg);
    when(pg.getLayout()).thenReturn(layoutContent);
    Optional<String> placementViewForLayout = testling.getPlacementViewForLayout(page, "header");
    assertThat(placementViewForLayout).contains("asSpecialLayoutView");
  }

  @Test
  public void getPlacementView() throws Exception {
    //default
    Optional<String> placementViewDefault = testling.getPlacementView(page, "header");
    assertThat(placementViewDefault).contains("asDefaultFragment");

    //custom layout config
    Content layoutContent = loadContentFor(202);
    PageGrid pg = mock(PageGrid.class);
    when(page.getPageGrid()).thenReturn(pg);
    when(pg.getLayout()).thenReturn(layoutContent);
    Optional<String> placementViewCustom = testling.getPlacementView(page, "header");
    assertThat(placementViewCustom).contains("asSpecialLayoutView");
  }

  private <T> T loadBeanFor(int contentId, Class<T> expectedType) {
    Content content = loadContentFor(contentId);
    return contentBeanFactory.createBeanFor(content, expectedType);
  }

  private Content loadContentFor(int contentId) {
    return contentRepository.getContent(IdHelper.formatContentId(contentId));
  }

  @Configuration
  @ImportResource(
          value = {
                  "classpath:/framework/spring/blueprint-contentbeans.xml",
                  "classpath:/META-INF/coremedia/livecontext-resolver.xml"
          },
          reader = ResourceAwareXmlBeanDefinitionReader.class
  )
  public static class LocalConfig {

    @Bean
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig(PrefetchFragmentsConfigReaderTest.CONTENT_REPOSITORY_URL);
    }

  }
}
