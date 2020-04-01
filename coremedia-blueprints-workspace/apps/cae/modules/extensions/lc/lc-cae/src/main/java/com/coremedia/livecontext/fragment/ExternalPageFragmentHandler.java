package com.coremedia.livecontext.fragment;

import com.coremedia.cap.multisite.SiteHelper;
import com.coremedia.blueprint.base.navigation.context.ContextStrategy;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.user.User;
import com.coremedia.livecontext.contentbeans.CMExternalPage;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.UserVariantHelper;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javax.servlet.http.HttpServletRequest;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ExternalPageFragmentHandler extends FragmentHandler {

  private static final String LIVECONTEXT_MANAGE_NAVIGATION = "livecontext.manageNavigation";
  private ContextStrategy<String, Navigation> contextStrategy;

  private boolean fullPageInheritance = true;
  private SettingsService settingsService;

  private String navigationViewName;

  @Nullable
  @Override
  ModelAndView createModelAndView(@NonNull FragmentParameters params, @NonNull HttpServletRequest request) {
    String pageId = params.getPageId();

    Site site = SiteHelper.getSiteFromRequest(request);
    if (site == null) {
      return HandlerHelper.notFound("Cannot derive a site from the request for page " + pageId);
    }

    Content siteRootDocument = site.getSiteRootDocument();
    CMChannel rootChannel = getContentBeanFactory().createBeanFor(siteRootDocument, CMChannel.class);
    if (rootChannel == null) {
      return HandlerHelper.notFound("Site " + site.getName() + " for page " + pageId + " has no root channel");
    }

    // Find a suitable context for the page in question.
    // I.e. a corresponding external page or some parent context
    // determined by the contextStrategy.
    Navigation context = contextStrategy.findAndSelectContextFor(pageId, rootChannel);
    if (context == null) {
      // Fallback to rootChannel if the page cannot be found
      context = rootChannel;
    }

    User developer = UserVariantHelper.getUser(request);
    String placement = params.getPlacement();

    // if the target is a channel and no placement and/or view is given we use a well-known "pagegrid" view that
    // results in a full page grid rendering (but without complete html head and body)
    String view = params.getView();
    if (view == null) {
      Content content = context.getContext().getContent();
      view = normalizedPageFragmentView(content, placement, null);
    }

    if (isNullOrEmpty(placement)) {
      // No placement means that a complete page is requested.
      // Either the context is this page, or we have no such page.
      if (fullPageInheritance || isTheExternalPage(context, pageId)) {
        // CAE will not manage the shop navigation if
        // the flag manageNavigation is disabled.
        // so return 404 when the navigation view for the root channel is requested.
        if (navigationViewName != null &&
                navigationViewName.equals(params.getView())
                && context.equals(rootChannel)
                && !isNavigationManaged(site)) {
          return HandlerHelper.notFound("Navigation is not managed by CoreMedia CAE");
        } else {
          return createFragmentModelAndView(context, view, rootChannel, developer);
        }
      } else {
        return HandlerHelper.notFound("No explicit augmented page found for id " + pageId);
      }
    } else {
      // Only a particular fragment is requested.
      return createFragmentModelAndViewForPlacementAndView(context, placement, view, rootChannel, developer);
    }
  }

  private boolean isNavigationManaged(@NonNull Site site) {
    if (settingsService == null) {
      return false;
    }

    return settingsService.getSetting(LIVECONTEXT_MANAGE_NAVIGATION, Boolean.class, site).orElse(false);
  }

  @Override
  public boolean include(@NonNull FragmentParameters params) {
    boolean isNotCatalogPage = params.getPageId() != null &&
            isNullOrEmpty(params.getProductId()) &&
            isNullOrEmpty(params.getCategoryId());

    String externalRef = params.getExternalRef();
    return isNotCatalogPage && (externalRef == null || !externalRef.startsWith("cm-"));
  }

  // ------------ Config --------------------------------------------

  @Required
  public void setContextStrategy(ContextStrategy<String, Navigation> contextStrategy) {
    this.contextStrategy = contextStrategy;
  }

  /**
   * Control the inheritance for complete pages.
   * <p>
   * If you request a placement, it is eventually inherited from a parent page.
   * If you request a complete page however, there are different usecases.
   * In a content centric scenario, you want exactly the requested page or notFound.
   * In a view centric scenario, parent pages are appropriate, if the
   * particular page does not exist.
   * <p>
   * Default is true.  (Backward compatible to older versions without this flag.)
   */
  public void setFullPageInheritance(boolean fullPageInheritance) {
    this.fullPageInheritance = fullPageInheritance;
  }

  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  public void setNavigationViewName(String navigationViewName) {
    this.navigationViewName = navigationViewName;
  }

  // --- internal ---------------------------------------------------

  /**
   * Checks whether the navigation represents the external page of the given id.
   */
  private static boolean isTheExternalPage(Navigation navigation, String pageId) {
    return navigation instanceof CMExternalPage && pageId.equals(((CMExternalPage) navigation).getExternalId());
  }
}
