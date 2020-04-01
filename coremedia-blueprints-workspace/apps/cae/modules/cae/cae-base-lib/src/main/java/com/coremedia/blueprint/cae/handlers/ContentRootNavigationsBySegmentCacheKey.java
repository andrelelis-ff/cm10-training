package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.base.links.UrlPathFormattingHelper;
import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.coremedia.cap.common.CapObjectDestroyedException;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SiteDestroyedException;
import com.coremedia.cap.multisite.SitesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link com.coremedia.cache.CacheKey} to compute a map from segment names to root navigation content objects
 */
class ContentRootNavigationsBySegmentCacheKey extends CacheKey<Map<String, Content>> {
  private static final Logger LOG = LoggerFactory.getLogger(ContentRootNavigationsBySegmentCacheKey.class);

  private final SitesService sitesService;
  private final UrlPathFormattingHelper urlPathFormattingHelper;

  ContentRootNavigationsBySegmentCacheKey(SitesService sitesService, UrlPathFormattingHelper urlPathFormattingHelper) {
    this.sitesService = sitesService;
    this.urlPathFormattingHelper = urlPathFormattingHelper;
  }

  @Override
  public Map<String, Content> evaluate(Cache cache) {
    final List<Content> rootNavigations = getRootNavigations();
    final Map<String, Content> result = new HashMap<>(rootNavigations.size());
    for (Content rootNavigationContent : rootNavigations) {
      try {
        result.put(urlPathFormattingHelper.getVanityName(rootNavigationContent), rootNavigationContent);
      } catch (CapObjectDestroyedException e) {
        LOG.debug("ignoring destroyed content '{}'", rootNavigationContent.getId(), e);
      }
    }
    return Collections.unmodifiableMap(result);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ContentRootNavigationsBySegmentCacheKey that = (ContentRootNavigationsBySegmentCacheKey) o;
    return sitesService.equals(that.sitesService) && urlPathFormattingHelper.equals(that.urlPathFormattingHelper);
  }

  @Override
  public int hashCode() {
    return 31 * sitesService.hashCode() + urlPathFormattingHelper.hashCode();
  }

  private List<Content> getRootNavigations() {
    List<Content> result = new ArrayList<>();
    for (Site site : sitesService.getSites()) {
      try {
        Content siteRootDocument = site.getSiteRootDocument();
        if (siteRootDocument != null) {
          result.add(siteRootDocument);
        }
      } catch (CapObjectDestroyedException | SiteDestroyedException e) {
        LOG.debug("ignoring destroyed site '{}'", site.getId(), e);
      }
    }
    return result;
  }
}
