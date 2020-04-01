package com.coremedia.livecontext.p13n.include;

import com.coremedia.blueprint.cae.layout.ContentBeanBackedPageGridPlacement;
import com.coremedia.blueprint.common.layout.DynamicContainerStrategy;
import com.coremedia.objectserver.view.RenderNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class P13NPlacementPredicateTest {

  @InjectMocks
  private P13NPlacementPredicate testling;

  @Mock
  private DynamicContainerStrategy dynamicContainerStrategy;

  @Before
  public void setUp() {
    when(dynamicContainerStrategy.isEnabled(any())).thenReturn(true);
    when(dynamicContainerStrategy.isDynamic(anyList())).thenReturn(true);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
  }

  @After
  public void teardown() {
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  public void testInputNull() {
    assertThat(testling.apply(null)).isFalse();
  }

  @Test
  public void testInputNotMatching() {
    RenderNode input = mock(RenderNode.class);
    when(input.getBean()).thenReturn(new Object());
    assertThat(testling.apply(input)).isFalse();
  }

  @Test
  public void testStrategyIsUnset() {
    RenderNode input = mock(RenderNode.class);
    when(input.getBean()).thenReturn(mock(ContentBeanBackedPageGridPlacement.class));
    when(dynamicContainerStrategy.isDynamic(anyList())).thenReturn(false);
    assertThat(testling.apply(input)).isFalse();
  }

  @Test
  public void testInputMatchingNoView() {
    RenderNode input = mock(RenderNode.class);
    when(input.getBean()).thenReturn(mock(ContentBeanBackedPageGridPlacement.class));
    when(input.getView()).thenReturn(null);
    assertThat(testling.apply(input)).isTrue();
  }

  @Test
  public void testInputMatchingAndFragmentPreviewSet() {
    RenderNode input = mock(RenderNode.class);
    when(input.getView()).thenReturn("fragmentPreview");
    assertThat(testling.apply(input)).isFalse();
  }

  @Test
  public void testInputMatchingAndMultiViewPreviewSet() {
    RenderNode input = mock(RenderNode.class);
    when(input.getView()).thenReturn("multiViewPreview");
    assertThat(testling.apply(input)).isFalse();
  }

  @Test
  public void testInputMatchingAndAsPreviewSet() {
    RenderNode input = mock(RenderNode.class);
    when(input.getView()).thenReturn("asPreview");
    assertThat(testling.apply(input)).isFalse();
  }

  @Test
  public void testInputMatchingOtherViewSet() {
    RenderNode input = mock(RenderNode.class);
    when(input.getBean()).thenReturn(mock(ContentBeanBackedPageGridPlacement.class));
    when(input.getView()).thenReturn("any_view_except_fragmentPreview");
    assertThat(testling.apply(input)).isTrue();
  }
}
