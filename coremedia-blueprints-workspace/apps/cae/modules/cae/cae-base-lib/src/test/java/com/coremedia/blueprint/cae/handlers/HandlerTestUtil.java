package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.objectserver.web.HandlerHelper;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Utilities for Handler and Linkscheme tests
 */
public final class HandlerTestUtil {

  // static utility class
  private HandlerTestUtil() {
  }

  // --- check utilities --------------------------------------------

  /**
   * Check that the mav holds a Page, return the Page.
   */
  public static Page extractPage(ModelAndView mav) {
    assertNotNull("null ModelAndView", mav);
    Object self = HandlerHelper.getRootModel(mav);
    assertNotNull("null self", self);
    assertTrue("not a page", self instanceof Page);
    return (Page) self;
  }

  /**
   * Check if a Page model consists of the expected content and channel.
   * <p>
   * Only applicable for content backed Pages.
   */
  public static void checkPage(ModelAndView mav, int contentId, int channelId) {
    Page page = extractPage(mav);
    Object content = page.getContent();
    assertNotNull("null content", content);
    assertEquals("wrong content", contentId, ((CMLinkable)content).getContentId());
    Navigation navigation = page.getNavigation();
    assertNotNull("null navigation", navigation);
    assertEquals("wrong navigation", channelId, ((CMNavigation)navigation).getContentId());
  }

  /**
   * Check if the model represents the expected Navigation.
   */
  public static void checkNavigation(ModelAndView mav, int channelId) {
    Object self = HandlerHelper.getRootModel(mav);
    assertNotNull("null self", self);
    assertTrue("not a CMNavigation", self instanceof CMNavigation);
    assertEquals("wrong navigation", channelId, ((CMNavigation) self).getContentId());
  }

  /**
   * Check if the model represents the expected class.
   */
  public static void checkModelAndView(ModelAndView mav, String expectedView, Class<?> clazz) {
    Object self = HandlerHelper.getRootModel(mav);
    assertNotNull("null self", self);
    assertTrue("not a " + clazz.getName(), clazz.isInstance(self));
    checkView(mav, expectedView);
  }

  /**
   * Check for the expected view.
   */
  public static void checkView(ModelAndView mav, String expectedView) {
    String view = mav.getViewName();
    if (expectedView==null) {
      // null and DEFAULT are equivalent and normalized to null during view dispatching anyway.
      assertTrue("wrong view: " + view, view==null || HandlerHelper.VIEWNAME_DEFAULT.equals(view));
    } else {
      assertTrue("wrong view: " + view, expectedView.equals(view));
    }
  }

  /**
   * Check if the model represents an HttpError.
   */
  public static void checkError(ModelAndView mav, int errorCode) {
    assertTrue(HandlerHelper.isError(mav, errorCode));
  }

}
