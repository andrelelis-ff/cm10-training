package com.coremedia.blueprint.assets.cae.handlers;


import com.coremedia.blueprint.assets.cae.TaxonomyOverview;
import com.coremedia.blueprint.cae.view.HashBasedFragmentHandler;
import com.coremedia.objectserver.view.RenderNode;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PaginatedSubjectAssetsPredicateTest {

  @Test
  public void apply() {
    RenderNode node = mock(RenderNode.class);
    TaxonomyOverview overview = mock(TaxonomyOverview.class);
    when(node.getBean()).thenReturn(overview);
    when(node.getView()).thenReturn(DownloadPortalHandler.ASSETS_VIEW);

    PaginatedSubjectAssetsPredicate predicate = new PaginatedSubjectAssetsPredicate();
    assertTrue(predicate.apply(node));
  }

  @Test
  public void applyNotPossible() {
    RenderNode node = mock(RenderNode.class);
    Object overview = mock(Object.class);
    when(node.getBean()).thenReturn(overview);
    when(node.getView()).thenReturn("anyView");

    PaginatedSubjectAssetsPredicate predicate = new PaginatedSubjectAssetsPredicate();
    assertFalse(predicate.apply(node));
  }

  @Test
  public void getDynamicInclude() {
    PaginatedSubjectAssetsPredicate predicate = new PaginatedSubjectAssetsPredicate();
    HashBasedFragmentHandler handler = predicate.getDynamicInclude(new Object(), "test");
    assertNotNull(handler);
    assertTrue(handler.getValidParameters().contains(DownloadPortalHandler.SUBJECT_REQUEST_PARAMETER_NAME));
    assertTrue(handler.getValidParameters().contains(DownloadPortalHandler.PAGE_REQUEST_PARAMETER_NAME));
  }
}
