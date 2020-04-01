package com.coremedia.livecontext.contentbeans;

import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.blueprint.cae.contentbeans.PageImpl;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.layout.PageGrid;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cache.Cache;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.context.LiveContextNavigation;
import com.coremedia.livecontext.navigation.LiveContextCategoryNavigation;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import com.google.common.annotations.VisibleForTesting;

/**
 * Custom page that renders the PDP page grid instead of the regular page grid.
 * <p>
 * Does not work with arbitrary Navigations, but only with LiveContextExternalChannels.
 */
public class ProductDetailPage extends PageImpl {
  @VisibleForTesting  // use the "pdpPage" factory bean
  public ProductDetailPage(boolean developerMode, SitesService sitesService, Cache cache,
                           TreeRelation<Content> contentTreeRelation,
                           ContentBeanFactory contentBeanFactory,
                           DataViewFactory dataViewFactory) {
    super(developerMode, sitesService, cache, contentTreeRelation, contentBeanFactory, dataViewFactory);
  }

  /**
   * Better use setLiveContextNavigation for type safety.
   *
   * @param navigation must be a LiveContextNavigation
   */
  @Override
  public final void setNavigation(Navigation navigation) {
    if (!(navigation instanceof LiveContextNavigation)) {
      throw new IllegalArgumentException("Navigation " + navigation + " is no LiveContextNavigation.  Use setLiveContextNavigation in order to avoid this kind of mismatch.");
    }
    setLiveContextNavigation((LiveContextNavigation)navigation);
  }

  public void setLiveContextNavigation(LiveContextNavigation navigation) {
    super.setNavigation(navigation);
  }

  @Override
  public PageGrid getPageGrid() {
    Object content = getContent();
    if (content instanceof LiveContextExternalProduct) {
      // The product is augmented.
      return ((LiveContextExternalProduct)content).getPageGrid();
    }
    // If the product is not augmented ask the category for the PDP pagegrid.
    Navigation navigation = getNavigation();
    if (navigation instanceof LiveContextExternalChannel) {
      // The category is augmented.
      return ((LiveContextExternalChannel)navigation).getPdpPagegrid();
    }
    if (navigation instanceof LiveContextCategoryNavigation) {
      // The category is not augmented.
      CMContext context = navigation.getContext();
      if (context instanceof LiveContextExternalChannel) {
        return ((LiveContextExternalChannel) context).getPdpPagegrid();
      }
    }
    throw new IllegalStateException("No pagegrid found for " + content);
  }

}
