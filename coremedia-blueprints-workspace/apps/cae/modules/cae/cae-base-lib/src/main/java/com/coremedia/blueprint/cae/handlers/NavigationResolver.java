package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.base.links.UrlPathFormattingHelper;
import com.coremedia.blueprint.cae.constants.RequestAttributeConstants;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.base.navigation.context.finder.TopicpageContextFinder;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.cap.content.Content;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

public class NavigationResolver {

  private NavigationSegmentsUriHelper navigationSegmentsUriHelper;
  private UrlPathFormattingHelper urlPathFormattingHelper;
  private ContextHelper contextHelper;
  private TopicpageContextFinder topicPageContextFinder;


  // --- configuration ----------------------------------------------

  @Required
  public void setNavigationSegmentsUriHelper(NavigationSegmentsUriHelper navigationSegmentsUriHelper) {
    this.navigationSegmentsUriHelper = navigationSegmentsUriHelper;
  }

  @Required
  public void setContextHelper(ContextHelper contextHelper) {
    this.contextHelper = contextHelper;
  }

  @Required
  public void setTopicPageContextFinder(TopicpageContextFinder topicPageContextFinder) {
    this.topicPageContextFinder = topicPageContextFinder;
  }

  @Required
  public void setUrlPathFormattingHelper(UrlPathFormattingHelper urlPathFormattingHelper) {
    this.urlPathFormattingHelper = urlPathFormattingHelper;
  }

  // --- features ---------------------------------------------------

  /**
   * Returns the navigation of the given navigationPath (a list of segment values).
   * Returns null if the navigation of the given navigationPath is not a context of the linkable.
   * If the linkable itself is a navigation, its segment must be the last element of the navigationPath.
   * If the linkable is not a navigation, its segment must not be included in the navigationPath.
   */
  public Navigation getNavigation(CMLinkable linkable, List<String> navigationPath) {
    if (linkable instanceof CMTaxonomy) {
      return navigationForTaxonomy((CMTaxonomy) linkable, navigationPath);
    } else {
      return navigationForLinkable(linkable, navigationPath);
    }
  }


  // --- internal ---------------------------------------------------

  private Navigation navigationForLinkable(CMLinkable linkable, List<String> navigationPath) {
    Navigation navigation = navigationSegmentsUriHelper.parsePath(navigationPath);
    List<? extends CMContext> contexts = linkable.getContexts();
    //noinspection SuspiciousMethodCalls
    if (contexts != null && contexts.contains(navigation)) {
      return navigation;
    }
    return null;
  }

  private CMNavigation navigationForTaxonomy(CMTaxonomy taxonomy, List<String> navigationPath) {
    //store content bean for dynamic query lists
    setPageModelToRequestConstants(taxonomy);

    // the navigation path must look as created by #buildLinkForTaxonomy:
    // /site/taxonomychannel
    if (isEmpty(navigationPath) || navigationPath.size() != 2) {
      return null;
    }
    CMNavigation rootContext = navigationSegmentsUriHelper.lookupRootSegment(navigationPath.get(0));
    if (!(rootContext instanceof CMContext)) {
      // Cannot happen in the default Blueprint.
      // CMNavigation and CMContext should be consolidated in CM8 (Jira Ticket: BLUEPRINT-7)
      rootContext = null;
    }
    CMNavigation topicpageChannel = contextHelper.findAndSelectContextFor((CMContext) rootContext, taxonomy);
    if (topicpageChannel == null || !navigationPath.get(1).equals(getDefaultTopicpageSegment(rootContext, taxonomy))) {
      // Navigation path is invalid wrt. taxonomy.  Outdated or faked URL.
      return null;
    }
    return topicpageChannel;
  }

  @VisibleForTesting
  void setPageModelToRequestConstants(CMTaxonomy taxonomy) {
    RequestAttributeConstants.setPageModel(taxonomy);
  }

  private String getDefaultTopicpageSegment(Navigation siteContext, CMTaxonomy taxonomy) {
    if (!(siteContext instanceof CMNavigation)) {
      return null;
    }
    Content defaultTopicPageChannel = topicPageContextFinder.findDefaultTopicpageChannelFor(taxonomy.getContent(), ((CMNavigation) siteContext).getContent());
    return defaultTopicPageChannel == null ? null : urlPathFormattingHelper.getVanityName(defaultTopicPageChannel);
  }


}
