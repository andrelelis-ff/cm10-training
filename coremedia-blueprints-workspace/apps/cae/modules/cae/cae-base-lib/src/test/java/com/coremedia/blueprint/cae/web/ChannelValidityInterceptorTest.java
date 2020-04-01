package com.coremedia.blueprint.cae.web;

import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.blueprint.cae.exception.InvalidContentException;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.cap.common.InvalidIdException;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.objectserver.web.HandlerHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.mockito.verification.VerificationMode;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChannelValidityInterceptorTest {

  private ChannelValidityInterceptor interceptor;

  @Mock
  private TreeRelation<Content> treeRelation;

  @Mock
  private Page page;

  @Mock
  private ModelAndView modelAndView;

  @Before
  public void setUp() throws Exception {
    interceptor = new ChannelValidityInterceptor();
    interceptor.setTreeRelation(treeRelation);
  }

  @Test
  public void testNoopModelAndViewEmpty() {
    triggerInterceptor(null);
    verifyNoCheckIfInGlobalNavigation();
  }

  @Test
  public void testNoopRootModelNotPage() {
    when(modelAndView.getModel()).thenReturn(getModelMapWithModelRoot(new Object()));
    triggerInterceptor(modelAndView);
    verifyNoCheckIfInGlobalNavigation();
  }

  @Test
  public void testNoopPageContentNotChannel() {
    when(page.getContent()).thenReturn(new Object());
    when(modelAndView.getModel()).thenReturn(getModelMapWithModelRoot(page));
    triggerInterceptor(modelAndView);
    verifyNoCheckIfInGlobalNavigation();
  }

  @Test
  public void testNoopChannelValid() {
    setUpTreeRelation(true);
    Content content = mock(Content.class);
    ContentType contentType = mock(ContentType.class);
    CMChannel cmChannel = mock(CMChannel.class);
    when(cmChannel.getContent()).thenReturn(content);
    when(page.getContent()).thenReturn(cmChannel);
    when(content.getType()).thenReturn(contentType);
    when(contentType.getName()).thenReturn("CMChannel");
    when(modelAndView.getModel()).thenReturn(getModelMapWithModelRoot(page));
    triggerInterceptor(modelAndView);
    verifyCheckIfInGlobalNavigation();
  }

  @Test(expected = InvalidContentException.class)
  public void testChannelNotPartOfNavigation() {
    setUpTreeRelation(false);
    Content content = mock(Content.class);
    ContentType contentType = mock(ContentType.class);
    CMChannel cmChannel = mock(CMChannel.class);
    when(page.getContent()).thenReturn(cmChannel);
    when(cmChannel.getContent()).thenReturn(content);
    when(content.getType()).thenReturn(contentType);
    when(contentType.getName()).thenReturn("CMChannel");
    when(modelAndView.getModel()).thenReturn(getModelMapWithModelRoot(page));
    triggerInterceptor(modelAndView);
    verifyCheckIfInGlobalNavigation();
  }

  @Test
  public void testChannelNoopNoCmChannel() {
    setUpTreeRelation(false);
    Content content = mock(Content.class);
    ContentType contentType = mock(ContentType.class);
    CMChannel cmChannel = mock(CMChannel.class);
    when(page.getContent()).thenReturn(cmChannel);
    when(cmChannel.getContent()).thenReturn(content);
    when(content.getType()).thenReturn(contentType);
    when(contentType.getName()).thenReturn("CMExternalChannel");
    when(modelAndView.getModel()).thenReturn(getModelMapWithModelRoot(page));
    triggerInterceptor(modelAndView);
    verifyNoCheckIfInGlobalNavigation();
  }

  private void setUpTreeRelation(boolean partOfGlobalNavigation) {
    Content root = mock(Content.class);
    final List<Content> pathToRoot = new ArrayList<>();
    pathToRoot.add(root);
    when(treeRelation.pathToRoot(any(Content.class))).thenAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
        return pathToRoot;
      }
    });
    when(treeRelation.isRoot(root)).thenReturn(partOfGlobalNavigation);
  }

  private void verifyCheckIfInGlobalNavigation(VerificationMode verificationMode) {
    verify(treeRelation, verificationMode).pathToRoot(any(Content.class));
    verify(treeRelation, verificationMode).isRoot(any(Content.class));
  }

  private void verifyCheckIfInGlobalNavigation () {
    verifyCheckIfInGlobalNavigation(times(1));
  }

  private void verifyNoCheckIfInGlobalNavigation() {
    verifyCheckIfInGlobalNavigation(never());
  }

  private void triggerInterceptor(ModelAndView modelAndView) throws InvalidIdException {
    interceptor.postHandle(null, null, null, modelAndView);
  }

  private Map<String, Object> getModelMapWithModelRoot(Object self) {
    Map<String, Object> result = new HashMap<>();
    result.put(HandlerHelper.MODEL_ROOT, self);
    return result;
  }
}
