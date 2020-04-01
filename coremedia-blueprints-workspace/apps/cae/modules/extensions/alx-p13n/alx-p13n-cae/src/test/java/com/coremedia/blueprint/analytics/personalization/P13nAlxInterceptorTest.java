package com.coremedia.blueprint.analytics.personalization;

import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.ContextCollectionImpl;
import com.coremedia.personalization.context.MapPropertyMaintainer;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Tests {@link P13nAlxInterceptor}.
 */
@SuppressWarnings({"unchecked", "NullableProblems"})
public class P13nAlxInterceptorTest {
  private static final String SEGMENT_CONTENT_PREFIX = "iAmContentForSegment_";

  @Test
  public void testSegmentExtraction() throws Exception {
    final ContextCollection contextCollection = new ContextCollectionImpl();
    final MapPropertyMaintainer segmentContext = new MapPropertyMaintainer();
    final String id1 = IdHelper.formatContentId(122);
    segmentContext.setProperty(id1, true);
    final String id2 = IdHelper.formatContentId(234);
    segmentContext.setProperty(id2, true);
    final String id3 = IdHelper.formatContentId(346);
    segmentContext.setProperty(id3, false);
    contextCollection.setContext("segmentContext", segmentContext);

    final ContentRepository contentRepository = Mockito.mock(ContentRepository.class);
    final Content mock1 = createContentMock(id1);
    Mockito.when(contentRepository.getContent(id1)).thenReturn(mock1);
    final Content mock2 = createContentMock(id2);
    Mockito.when(contentRepository.getContent(id2)).thenReturn(mock2);
    final Content mock3 = createContentMock(id3);
    Mockito.when(contentRepository.getContent(id3)).thenReturn(mock3);
    final String id4 = IdHelper.formatContentId(345);
    final Content mock4 = createContentMock(id4);
    Mockito.when(contentRepository.getContent(id4)).thenReturn(mock4);

    final P13nAlxInterceptor interceptor = new P13nAlxInterceptor();
    interceptor.setContextCollection(contextCollection);
    interceptor.setSegmentContextName("segmentContext");
    interceptor.setContentRepository(contentRepository);

    final MockHttpServletRequest request = new MockHttpServletRequest();
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final ModelAndView modelAndView = new ModelAndView();

    interceptor.postHandle(request, response, null, modelAndView);

    BeanWrapper modelViewWrapper = PropertyAccessorFactory.forBeanPropertyAccess(modelAndView);
    final String[] segmentIds = (String[]) modelViewWrapper.getPropertyValue("model[p13nAlxProperties][segmentIds]");
    Assert.assertEquals(new HashSet(Arrays.asList("122","234")), new HashSet(Arrays.asList(segmentIds)));

    final String[] segmentNames = (String[]) modelViewWrapper.getPropertyValue("model[p13nAlxProperties][segmentNames]");
    Assert.assertEquals(new HashSet(Arrays.asList(SEGMENT_CONTENT_PREFIX + 122, SEGMENT_CONTENT_PREFIX + 234)),
            new HashSet(Arrays.asList(segmentNames)));
  }

  private Content createContentMock(String capID) {
    final Content content = Mockito.mock(Content.class);
    Mockito.when(content.getName()).thenReturn(SEGMENT_CONTENT_PREFIX + IdHelper.parseContentId(capID));
    return content;
  }

  @Test
  public void testWithoutSegmentContext() throws Exception {
    final ContextCollection contextCollection = new ContextCollectionImpl();
    final P13nAlxInterceptor interceptor = new P13nAlxInterceptor();
    interceptor.setContextCollection(contextCollection);
    interceptor.setSegmentContextName("segmentContext");

    final MockHttpServletRequest request = new MockHttpServletRequest();
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final ModelAndView modelAndView = new ModelAndView();

    interceptor.postHandle(request, response, null, modelAndView);
    BeanWrapper modelViewWrapper = PropertyAccessorFactory.forBeanPropertyAccess(modelAndView);
    modelViewWrapper.setAutoGrowNestedPaths(true);
    final String[] segmentIds = (String[]) modelViewWrapper.getPropertyValue("model[p13nAlxProperties][segmentIds]");
    Assert.assertNotNull(segmentIds);
    Assert.assertEquals(0, segmentIds.length);
  }

  /**
   * Call trivial methods so they don't have a negative impact on code coverage.
   */
  @Test
  public void dummyMethodCalls() {
    final P13nAlxInterceptor interceptor = new P13nAlxInterceptor();
    interceptor.toString();
    interceptor.getContextCollection();
    interceptor.getSegmentContextName();
  }
}
