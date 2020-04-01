package com.coremedia.blueprint.cae.web.links;

import com.coremedia.blueprint.common.navigation.Navigation;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * Supporting class to provide access to request local navigation-related objects, such as the
 * {@link #getNavigation current navigation context}.
 */
public final class NavigationLinkSupport {
  public static final String ATTR_NAME_CMNAVIGATION = NavigationLinkSupport.class.getName() + '.' + "cmnavigation";

  /**
   * Hide Utility Class Constructor
   */
  private NavigationLinkSupport() {
  }

  /**
   * Adds the {@link com.coremedia.blueprint.common.navigation.Navigation} to the modelAndView's attributes
   * using key {@link #ATTR_NAME_CMNAVIGATION}.
   *
   * @param modelAndView modelAndView to add navigation
   * @param navigation   navigation to register in the request
   */
  @SuppressWarnings({"unchecked"})
  public static void setNavigation(@NonNull ModelAndView modelAndView, Navigation navigation) {
    if (navigation != null) {
      modelAndView.addObject(ATTR_NAME_CMNAVIGATION, navigation);
    }
  }

  /**
   * Retrieves the {@link com.coremedia.blueprint.common.contentbeans.CMNavigation} from the
   * ModelAndView's parameter map using the key {@link #ATTR_NAME_CMNAVIGATION}.
   *
   * @param parameters Map
   * @return current navigation or <tt>null</tt> if none was set
   */
  public static Navigation getNavigation(Map parameters) {
    return (Navigation) parameters.get(ATTR_NAME_CMNAVIGATION);
  }
}
