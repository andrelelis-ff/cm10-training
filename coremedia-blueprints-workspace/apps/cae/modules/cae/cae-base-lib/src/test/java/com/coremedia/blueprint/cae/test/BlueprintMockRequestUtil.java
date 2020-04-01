package com.coremedia.blueprint.cae.test;

import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.objectserver.view.ViewUtils;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.ServletRequest;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Map;

public final class BlueprintMockRequestUtil {

  // static utility class
  private BlueprintMockRequestUtil() {
  }

  /**
   * Create a request with a context wrt. NavigationLinkSupport.
   */
  public static MockHttpServletRequest createRequestWithContext(CMNavigation navigation) {
    return createRequestWithContext(navigation, Collections.<String, String>emptyMap());
  }

  /**
   * Create a request with a context wrt. NavigationLinkSupport and ViewUtils parameters.
   */
  public static MockHttpServletRequest createRequestWithContext(CMNavigation navigation, Map<String, ?> viewutilsParams) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    setRequestWithContext(request, navigation, viewutilsParams);
    return request;
  }

  /**
   * Modifies a request with a context wrt. NavigationLinkSupport.
   */
  public static void setRequestWithContext(ServletRequest request, CMNavigation navigation) {
    setRequestWithContext(request, navigation, Collections.<String, String>emptyMap());
  }

  /**
   * Modifies a request with a context wrt. NavigationLinkSupport and ViewUtils parameters.
   */
  public static void setRequestWithContext(@NotNull ServletRequest request, CMNavigation navigation, Map<String, ?> viewutilsParams) {
    request.setAttribute(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION, navigation);
    request.setAttribute(ViewUtils.PARAMETERS, viewutilsParams);
  }
}
