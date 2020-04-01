package com.coremedia.livecontext.navigation;

import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.commercebeans.CategoryInSite;
import com.coremedia.livecontext.ecommerce.catalog.Category;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Immutable instances of CategoryInSite.
 */
public class CategoryInSiteImpl implements CategoryInSite {
  private final Category category;
  private final Site site;

  public CategoryInSiteImpl(@NonNull Category category, @NonNull Site site) {
    this.category = category;
    this.site = site;
  }

  @NonNull
  @Override
  public Category getCategory() {
    return category;
  }

  @NonNull
  @Override
  public Site getSite() {
    return site;
  }
}
