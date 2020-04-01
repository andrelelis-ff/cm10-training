package com.coremedia.blueprint.personalization.interceptors;

import com.coremedia.blueprint.cae.contentbeans.PageImpl;
import com.coremedia.blueprint.common.contentbeans.CMArticle;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.ContextCollectionImpl;
import com.coremedia.personalization.context.PropertyProfile;
import com.coremedia.personalization.context.PropertyProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * Tests {@link LastVisitedInterceptor}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
        "classpath:/com/coremedia/cae/contentbean-services.xml",
        "classpath:/com/coremedia/cae/dataview-services.xml",
        "classpath:/com/coremedia/cae/link-services.xml",
        "classpath:/com/coremedia/cache/cache-services.xml",
        "classpath:/com/coremedia/id/id-services.xml",
        "classpath:/com/coremedia/blueprint/personalization/p13n-xml-repo-context.xml",
        "classpath:/framework/spring/blueprint-contentbeans.xml",
        "classpath:/framework/spring/personalization-plugin/personalization-contentbeans.xml"
})
public final class LastVisitedInterceptorTest {
  private static final String CONTEXT_NAME = "lastVisitedIDs";
  public static final String PAGES_VISITED = "pagesVisited";

  @Inject
  private ContentBeanFactory contentBeanFactory;
  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private ContentRepository contentRepository;

  private LastVisitedInterceptor interceptor;
  private CMChannel channel;
  private CMArticle article;
  private CMArticle article2;
  private CMArticle article3;

  private Page page;

  private PropertyProfile context;

  @Before
  public void setUp() throws Exception {
    // set up the contextBean
    ContextCollection contextCollection = new ContextCollectionImpl();
    contextCollection.setContext(CONTEXT_NAME, new PropertyProfile());

    // set up the interceptor
    interceptor = new LastVisitedInterceptor();
    interceptor.setContextCollection(contextCollection);
    interceptor.setContextName(CONTEXT_NAME);
    interceptor.setListSize(2);

    channel = getContentBean(24);
    article = getContentBean(26);
    article2 = getContentBean(28);
    article3 = getContentBean(30);
    page = new PageImpl(channel, article, true, mock(SitesService.class), null, null, null, null);
    context = contextCollection.getContext(CONTEXT_NAME, PropertyProfile.class);

  }

  private <T> T getContentBean(int i) {
    return (T) contentBeanFactory.createBeanFor(contentRepository.getContent(IdHelper.formatContentId(i)), ContentBean.class);
  }

  //tests if the last requested ID is being stored
  @Test
  public void testLastIdStored() throws Exception {
    final ModelAndView modelAndView = new ModelAndView();
    modelAndView.addObject("self", page);

    context.clear();

    interceptor.postHandle(null, null, null, modelAndView);

    Assert.assertEquals(Arrays.asList(26), context.getProperty(PAGES_VISITED));

  }


  //tests if double entries are removed from the list
  @Test
  public void testDoubleEntry() throws Exception {
    final ModelAndView modelAndView = new ModelAndView();
    modelAndView.addObject("self", page);

    context.clear();

    interceptor.postHandle(null, null, null, modelAndView);
    interceptor.postHandle(null, null, null, modelAndView);

    assertEquals(Arrays.asList(26), context.getProperty(PAGES_VISITED));
  }

  //tests if the list extends the set limit
  @Test
  public void testListLimit() throws Exception {
    final ModelAndView modelAndView = new ModelAndView();
    modelAndView.addObject("self", page);

    interceptor.postHandle(null, null, null, modelAndView);

    page = new PageImpl(channel, article2, true, mock(SitesService.class), null, null, null, null);
    modelAndView.clear();
    modelAndView.addObject("self", page);
    interceptor.postHandle(null, null, null, modelAndView);

    page = new PageImpl(channel, article3, true, mock(SitesService.class), null, null, null, null);
    modelAndView.clear();
    modelAndView.addObject("self", page);
    interceptor.postHandle(null, null, null, modelAndView);

    assertEquals(Arrays.asList(30, 28), context.getProperty(PAGES_VISITED));
    final PropertyProfile.CoDec coDec = new PropertyProfile.CoDec();
    final Object o = coDec.contextFromString(coDec.stringFromContext(context));
    assertNotNull(o);
    PropertyProvider propertyProvider = (PropertyProvider) o;
    assertEquals(Arrays.asList(30, 28), propertyProvider.getProperty(PAGES_VISITED));
  }

  //tests if a "pageless" request is ignored
  @Test
  public void testIgnoreRequestWithoutPage() throws Exception {
    final ModelAndView modelAndView = new ModelAndView();
    modelAndView.addObject("self", article);

    context.clear();

    interceptor.postHandle(null, null, null, modelAndView);

    Assert.assertNull(context.getProperty(PAGES_VISITED));
  }

  //tests if a modelandview without a resolvabe object crashes
  @Test
  public void testEmptyObjectInModelAndView() throws Exception {
    final ModelAndView modelAndView = new ModelAndView();

    context.clear();

    interceptor.postHandle(null, null, null, modelAndView);

    Assert.assertNull(context.getProperty(PAGES_VISITED));
  }


}
