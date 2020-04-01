package com.coremedia.blueprint.personalization.include;

import com.coremedia.blueprint.personalization.contentbeans.CMP13NSearch;
import com.coremedia.blueprint.personalization.contentbeans.CMSelectionRules;
import com.coremedia.objectserver.view.RenderNode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class P13NIncludePredicateTest {

  P13NIncludePredicate testling;

  @Before
  public void setUp() throws Exception {
    testling = new P13NIncludePredicate();
  }

  @Test
  public void testInputNull() throws Exception {
    assertFalse(testling.apply(null));
  }

  @Test
  public void testInputNotMatching() throws Exception {
    RenderNode input = mock(RenderNode.class);
    when(input.getBean()).thenReturn(new Object());
    assertFalse(testling.apply(input));
  }

  @Test
  public void testInputMatchingNoView() throws Exception {
    RenderNode input = mock(RenderNode.class);
    when(input.getBean()).thenReturn(mock(CMSelectionRules.class));
    when(input.getView()).thenReturn(null);
    assertTrue(testling.apply(input));
  }

  @Test
  public void testInputMatchingAndFragmentPreviewSet() throws Exception {
    RenderNode input = mock(RenderNode.class);
    when(input.getBean()).thenReturn(mock(CMP13NSearch.class));
    when(input.getView()).thenReturn("fragmentPreview");
    assertFalse(testling.apply(input));
  }

  @Test
  public void testInputMatchingOtherViewSet() throws Exception {
    RenderNode input = mock(RenderNode.class);
    when(input.getBean()).thenReturn(mock(CMSelectionRules.class));
    when(input.getView()).thenReturn("any_view_except_fragmentPreview");
    assertTrue(testling.apply(input));
  }
}
