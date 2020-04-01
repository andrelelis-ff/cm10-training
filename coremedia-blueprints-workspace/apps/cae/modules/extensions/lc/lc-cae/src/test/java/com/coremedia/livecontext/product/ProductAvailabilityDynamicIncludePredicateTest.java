package com.coremedia.livecontext.product;

import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.objectserver.view.RenderNode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link com.coremedia.livecontext.product.ProductAvailabilityDynamicIncludePredicate}.
 */
public class ProductAvailabilityDynamicIncludePredicateTest {

  ProductAvailabilityDynamicIncludePredicate testling;

  @Before
  public void setUp() throws Exception {
    testling = new ProductAvailabilityDynamicIncludePredicate();
  }

  @Test
  public void testInputNull() throws Exception {
    assertFalse(testling.apply(null));
  }

  @Test
  public void testInputNotInstanceOfProductInSite() throws Exception {
    RenderNode input = mock(RenderNode.class);
    when(input.getBean()).thenReturn(new Object());
    assertFalse(testling.apply(input));
  }

  @Test
  public void testInputProductInSiteNoView() throws Exception {
    RenderNode input = mock(RenderNode.class);
    when(input.getBean()).thenReturn(mock(ProductInSite.class));
    when(input.getView()).thenReturn(null);
    assertFalse(testling.apply(input));
  }

  @Test
  public void testInputProductInSiteViewNotMatching() throws Exception {
    RenderNode input = mock(RenderNode.class);
    when(input.getBean()).thenReturn(mock(ProductInSite.class));
    when(input.getView()).thenReturn("i_do_not_match");
    assertFalse(testling.apply(input));
  }

  @Test
  public void testInputProductInSiteViewMatches() throws Exception {
    RenderNode input = mock(RenderNode.class);
    when(input.getBean()).thenReturn(mock(ProductInSite.class));
    when(input.getView()).thenReturn(ProductAvailabilityDynamicIncludePredicate.VIEW_NAME_AVAILABILITY_FRAGMENT);
    assertTrue(testling.apply(input));
  }

  @Test
  public void testInputNotProductInSiteViewMatching() throws Exception {
    RenderNode input = mock(RenderNode.class);
    when(input.getBean()).thenReturn(new Object());
    when(input.getView()).thenReturn(ProductAvailabilityDynamicIncludePredicate.VIEW_NAME_AVAILABILITY_FRAGMENT);
    assertFalse(testling.apply(input));
  }
}
