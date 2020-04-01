package com.coremedia.blueprint.personalization.preview;

import com.coremedia.cap.common.IdHelper;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.PropertyProvider;
import com.coremedia.personalization.context.collector.SegmentSource;
import com.coremedia.personalization.preview.PreviewPersonalizationHandlerInterceptor;
import com.coremedia.personalization.preview.TestContextSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
        "classpath:/com/coremedia/cae/contentbean-services.xml",
        "classpath:/com/coremedia/cae/dataview-services.xml",
        "classpath:/com/coremedia/cae/link-services.xml",
        "classpath:/com/coremedia/cache/cache-services.xml",
        "classpath:/com/coremedia/id/id-services.xml",
        "classpath:/com/coremedia/cae/dataview-services.xml",
        "classpath:/com/coremedia/cae/contentbean-services.xml",
        "classpath:/com/coremedia/blueprint/personalization/p13n-xml-repo-context.xml",

        "classpath:/framework/spring/personalization-plugin/personalization-contentbeans.xml",
        "classpath:/framework/spring/personalization-plugin/personalization-context.xml",
        "classpath:/framework/spring/personalization-plugin/personalization-interceptors.xml",
        "classpath:/META-INF/coremedia/p13n-preview-cae-context.xml",
        "classpath:/com/coremedia/cae/handler-services.xml"
})
public class P13NPreviewContentTest {

  @Inject
  private BeanFactory beanFactory;
  @Inject
  private ContextCollection contextCollection;
  @Inject
  private SegmentSource segmentSource;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  //------------------------------------- Test user profile -------------------------------------

  @Before
  public void setup(){
    segmentSource.setPathToSegments("/");
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
  }

  @After
  public void teardown() {
    contextCollection.clear();
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  public void testNoUser() throws Exception {

    Assert.assertTrue("empty context collection", contextCollection.getContextNames().isEmpty());

    @SuppressWarnings("unchecked") final List<HandlerInterceptor> handlerInterceptors = (List<HandlerInterceptor>) beanFactory.getBean("handlerInterceptors");
    Assert.assertFalse("no interceptors found", handlerInterceptors.isEmpty());

    for(HandlerInterceptor hi : handlerInterceptors) {
      hi.preHandle(request, response, this);
    }

    ModelAndView modelAndView = new ModelAndView();
    for(HandlerInterceptor hi : handlerInterceptors) {
      hi.postHandle(request, response, this, modelAndView);
    }

    // segment
    String contextName = "segment";
    PropertyProvider propertyProvider = contextCollection.getContext(contextName, PropertyProvider.class);
    Assert.assertFalse(contextName, propertyProvider.getProperty(IdHelper.formatContentId(34),true));
    Assert.assertFalse(contextName, propertyProvider.getProperty(IdHelper.formatContentId(32),true));

    // system
    propertyProvider = contextCollection.getContext("system", PropertyProvider.class);
    Assert.assertFalse("system: " + propertyProvider, propertyProvider.getPropertyNames().isEmpty());

    // taxonomy stuff should be null or empty
    propertyProvider = contextCollection.getContext("explicit", PropertyProvider.class);
    Assert.assertTrue("explicit: " + propertyProvider, propertyProvider == null || propertyProvider.getPropertyNames().isEmpty());

  }

  @Test
  public void testUser() throws Exception {

    Assert.assertTrue("empty context collection", contextCollection.getContextNames().isEmpty());

    request.setParameter(TestContextSource.QUERY_PARAMETER_TESTCONTEXTID, ""+42);
    request.setParameter(PreviewPersonalizationHandlerInterceptor.QUERY_PARAMETER_TESTCONTEXT,"");

    @SuppressWarnings("unchecked") final List<HandlerInterceptor> handlerInterceptors = (List<HandlerInterceptor>) beanFactory.getBean("handlerInterceptors");
    for(HandlerInterceptor hi : handlerInterceptors) {
      hi.preHandle(request, response, this);
    }

    ModelAndView modelAndView = new ModelAndView();
    for(HandlerInterceptor hi : handlerInterceptors) {
      hi.postHandle(request, response, this, modelAndView);
    }

    final Collection<String> contextNames = contextCollection.getContextNames();
    Assert.assertFalse("empty: " + contextNames, contextNames.isEmpty());

    // segment
    final PropertyProvider segment = contextCollection.getContext("segment", PropertyProvider.class);
    Assert.assertTrue("keyword segment "+ segment, segment.getProperty(IdHelper.formatContentId(34),false));
    Assert.assertTrue("taxonomy segment "+ segment, segment.getProperty(IdHelper.formatContentId(32),false));
  }

}
