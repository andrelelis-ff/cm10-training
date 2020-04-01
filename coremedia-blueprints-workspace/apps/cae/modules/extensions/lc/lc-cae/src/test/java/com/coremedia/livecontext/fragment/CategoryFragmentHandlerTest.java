package com.coremedia.livecontext.fragment;

import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.objectserver.web.HandlerHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CategoryFragmentHandlerTest extends FragmentHandlerTestBase<CategoryFragmentHandler> {

  @Test
  public void handleCategoryViewFragmentNoLiveContextNavigationFound() {
    when(getResolveContextStrategy().resolveContext(getSite(), category)).thenReturn(Optional.empty());
    ModelAndView result = getTestling().createModelAndView(getFragmentParameters4ProductWithCategory(), request);
    assertNotNull(result);
    assertTrue(HandlerHelper.isNotFound(result));
  }

  @Test
  public void handleCategoryViewFragment() {
    when(connection.getCatalogService().findCategoryById(any(CommerceId.class), any(StoreContext.class))).thenReturn(category);
    ModelAndView result = getTestling().createModelAndView(getFragmentParameters4ProductWithCategory(), request);
    assertDefaultPage(result);
    verifyDefault();
  }

  @Override
  protected CategoryFragmentHandler createTestling() {
    return new CategoryFragmentHandler();
  }

  @Before
  public void defaultSetup() {
    super.defaultSetup();
    getTestling().setContextStrategy(resolveContextStrategy);
    when(resolveContextStrategy.resolveContext(site, category)).thenReturn(Optional.of(navigation));
  }

  @After
  public void tearDown() {
    defaultTeardown();
  }
}
