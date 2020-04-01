package com.coremedia.livecontext.handler;

import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Objects;

class SearchLandingPageContextCacheKey extends CacheKey<Content> {

  private final SearchLandingPagesLinkBuilderHelper searchLandingPagesLinkBuilderHelper;
  private final Site site;

  SearchLandingPageContextCacheKey(@NonNull SearchLandingPagesLinkBuilderHelper searchLandingPagesLinkBuilderHelper,
                                   @NonNull Site site) {
    this.searchLandingPagesLinkBuilderHelper = searchLandingPagesLinkBuilderHelper;
    this.site = site;
  }

  @Override
  public Content evaluate(Cache cache) throws Exception {
    return searchLandingPagesLinkBuilderHelper.getNavigationContext(site);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SearchLandingPageContextCacheKey that = (SearchLandingPageContextCacheKey) o;
    return Objects.equals(searchLandingPagesLinkBuilderHelper, that.searchLandingPagesLinkBuilderHelper)
            && Objects.equals(site, that.site);
  }

  @Override
  public int hashCode() {
    return Objects.hash(searchLandingPagesLinkBuilderHelper, site);
  }
}
