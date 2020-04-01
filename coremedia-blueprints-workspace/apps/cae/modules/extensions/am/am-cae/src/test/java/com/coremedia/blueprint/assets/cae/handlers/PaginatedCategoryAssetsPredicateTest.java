package com.coremedia.blueprint.assets.cae.handlers;


import com.coremedia.blueprint.assets.cae.CategoryOverview;
import com.coremedia.blueprint.cae.view.HashBasedFragmentHandler;
import com.coremedia.objectserver.view.RenderNode;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PaginatedCategoryAssetsPredicateTest {

  @Test
  public void apply() {
    RenderNode node = mock(RenderNode.class);
    CategoryOverview overview = mock(CategoryOverview.class);
    when(node.getBean()).thenReturn(overview);
    when(node.getView()).thenReturn(DownloadPortalHandler.ASSETS_VIEW);

    PaginatedCategoryAssetsPredicate predicate = new PaginatedCategoryAssetsPredicate();
    assertTrue(predicate.apply(node));
  }

  @Test
  public void applyNotPossible() {
    RenderNode node = mock(RenderNode.class);
    Object overview = mock(Object.class);
    when(node.getBean()).thenReturn(overview);
    when(node.getView()).thenReturn("anyView");

    PaginatedCategoryAssetsPredicate predicate = new PaginatedCategoryAssetsPredicate();
    assertFalse(predicate.apply(node));
  }

  @Test
  public void getDynamicInclude() {
    PaginatedCategoryAssetsPredicate predicate = new PaginatedCategoryAssetsPredicate();
    HashBasedFragmentHandler handler = predicate.getDynamicInclude(new Object(), "test");
    assertNotNull(handler);
    assertTrue(handler.getValidParameters().contains(DownloadPortalHandler.CATEGORY_REQUEST_PARAMETER_NAME));
    assertTrue(handler.getValidParameters().contains(DownloadPortalHandler.PAGE_REQUEST_PARAMETER_NAME));
  }
}
