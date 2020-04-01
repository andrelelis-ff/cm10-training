package com.coremedia.blueprint.assets.cae.handlers;

import com.coremedia.blueprint.assets.cae.SearchOverview;
import com.coremedia.blueprint.cae.view.HashBasedFragmentHandler;
import com.coremedia.objectserver.view.RenderNode;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PaginatedSearchAssetsPredicateTest {

  @Test
  public void apply() {
    RenderNode node = mock(RenderNode.class);
    SearchOverview overview = mock(SearchOverview.class);
    when(node.getBean()).thenReturn(overview);
    when(node.getView()).thenReturn(DownloadPortalHandler.ASSETS_VIEW);

    PaginatedSearchAssetsPredicate predicate = new PaginatedSearchAssetsPredicate();
    assertTrue(predicate.apply(node));
  }

  @Test
  public void applyNotPossible() {
    RenderNode node = mock(RenderNode.class);
    Object overview = mock(Object.class);
    when(node.getBean()).thenReturn(overview);
    when(node.getView()).thenReturn("anyView");

    PaginatedSearchAssetsPredicate predicate = new PaginatedSearchAssetsPredicate();
    assertFalse(predicate.apply(node));
  }

  @Test
  public void getDynamicInclude() {
    PaginatedSearchAssetsPredicate predicate = new PaginatedSearchAssetsPredicate();
    HashBasedFragmentHandler handler = predicate.getDynamicInclude(new Object(), "test");
    assertNotNull(handler);
    assertTrue(handler.getValidParameters().contains(DownloadPortalHandler.SEARCH_REQUEST_PARAMETER_NAME));
    assertTrue(handler.getValidParameters().contains(DownloadPortalHandler.PAGE_REQUEST_PARAMETER_NAME));
  }
  
}
