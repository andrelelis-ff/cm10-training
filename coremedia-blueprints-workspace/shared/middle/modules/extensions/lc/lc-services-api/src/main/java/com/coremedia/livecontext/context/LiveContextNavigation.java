package com.coremedia.livecontext.context;

import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.ecommerce.catalog.Category;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * @cm.template.api
 */
public interface LiveContextNavigation extends Navigation {

  /**
   * Returns the category.
   *
   * @return the category
   * @cm.template.api
   */
  Category getCategory();

  /**
   * Returns the site.
   *
   * @return the site.
   */
  @NonNull
  Site getSite();
}
