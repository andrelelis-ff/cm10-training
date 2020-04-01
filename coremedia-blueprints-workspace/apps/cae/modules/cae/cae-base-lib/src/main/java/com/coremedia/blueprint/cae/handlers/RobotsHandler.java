package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.robots.RobotsBean;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.objectserver.web.HandlerHelper;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENT_ROOT;
import static com.coremedia.blueprint.links.BlueprintUriConstants.Prefixes.PREFIX_SERVICE;

/**
 * Handler to generate a configured robots.txt
 * for {@link com.coremedia.blueprint.common.robots.RobotsBean}
 */
@RequestMapping
public class RobotsHandler extends HandlerBase {

  public static final String SEGMENT_ROBOTS = "robots";

  /**
   * URI Pattern for robots txt
   * e.g. /robots/media
   * Redirect for a external URL like /media/robots.txt must be configured in an external application like Apache webserver.
   */
  private static final String URI_PATTERN =
          '/' + PREFIX_SERVICE +
                  '/' + SEGMENT_ROBOTS +
                  "/{" + SEGMENT_ROOT + '}';

  private NavigationSegmentsUriHelper navigationSegmentsUriHelper;
  private SettingsService settingsService;
  private SitesService sitesService;

  // --- spring config -------------------------------------------------------------------------------------------------

  @Required
  public void setNavigationSegmentsUriHelper(NavigationSegmentsUriHelper navigationSegmentsUriHelper) {
    this.navigationSegmentsUriHelper = navigationSegmentsUriHelper;
  }

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @Required
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }

  // --- Handlers ------------------------------------------------------------------------------------------------------

  /**
   * Handles a request for the robots.txt to this web presence
   */
  @GetMapping(value = URI_PATTERN)
  public ModelAndView handleRequest(@PathVariable(SEGMENT_ROOT) String segment) {
    CMNavigation rootNavigation = navigationSegmentsUriHelper.lookupRootSegment(segment);
    if (rootNavigation == null) {
      return HandlerHelper.notFound("unknown root channel [" + segment + "]");
    }
    RobotsBean robotsBean = new RobotsBean(rootNavigation, settingsService, sitesService);
    return HandlerHelper.createModel(robotsBean);
  }
}
