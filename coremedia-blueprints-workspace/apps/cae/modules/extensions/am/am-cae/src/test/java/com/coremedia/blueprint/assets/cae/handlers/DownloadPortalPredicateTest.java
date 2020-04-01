package com.coremedia.blueprint.assets.cae.handlers;


import com.coremedia.blueprint.assets.cae.DownloadPortal;
import com.coremedia.blueprint.cae.view.HashBasedFragmentHandler;
import com.coremedia.objectserver.view.RenderNode;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DownloadPortalPredicateTest {
  @Test
  public void apply() {
    RenderNode node = mock(RenderNode.class);
    DownloadPortal overview = mock(DownloadPortal.class);
    when(node.getBean()).thenReturn(overview);
    when(node.getView()).thenReturn(null);

    DownloadPortalPredicate predicate = new DownloadPortalPredicate();
    assertTrue(predicate.apply(node));
  }

  @Test
  public void applyNotPossible() {
    RenderNode node = mock(RenderNode.class);
    Object overview = mock(Object.class);
    when(node.getBean()).thenReturn(overview);
    when(node.getView()).thenReturn("anyView");

    DownloadPortalPredicate predicate = new DownloadPortalPredicate();
    assertFalse(predicate.apply(node));
  }

  @Test
  public void getDynamicInclude() {
    DownloadPortalPredicate predicate = new DownloadPortalPredicate();
    HashBasedFragmentHandler handler = predicate.getDynamicInclude(new Object(), "test");
    assertNotNull(handler);
    assertTrue(handler.getValidParameters().contains(DownloadPortalHandler.CATEGORY_REQUEST_PARAMETER_NAME));
    assertTrue(handler.getValidParameters().contains(DownloadPortalHandler.ASSET_REQUEST_PARAMETER_NAME));
  }
}
