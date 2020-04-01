package com.coremedia.livecontext.studio.asset;

import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.asset.AssetSearchService;
import com.coremedia.rest.cap.content.search.SearchService;
import com.coremedia.rest.cap.content.search.SearchServiceResult;
import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StudioAssetSearchService implements AssetSearchService {

  private static final Logger LOG = LoggerFactory.getLogger(StudioAssetSearchService.class);

  private static final int DEFAULT_CAPACITY = 10000;

  private final SearchService searchService;
  private final ContentRepository contentRepository;
  private final Cache cache;

  private long cacheForInSeconds = 300;

  StudioAssetSearchService(SearchService searchService, ContentRepository contentRepository, Cache cache) {
    this.searchService = searchService;
    this.contentRepository = contentRepository;
    this.cache = cache;
  }

  @NonNull
  @Override
  public List<Content> searchAssets(@NonNull String contentType, @NonNull String externalId, @NonNull Site site) {
    return cache.get(new SolrQueryCacheKey(contentType, externalId, site, cacheForInSeconds));
  }

  @PostConstruct
  void initialize() {
    long capacity = cache.getCapacity(SolrQueryCacheKey.CACHE_CLASS);

    if (capacity < 10) {
      LOG.info("configuring default capacity {} for cache key {} (must be at least 10 to avoid warnings)",
              DEFAULT_CAPACITY, SolrQueryCacheKey.CACHE_CLASS);
      cache.setCapacity(SolrQueryCacheKey.CACHE_CLASS, DEFAULT_CAPACITY);
    }
  }

  @NonNull
  private List<Content> doSearch(@NonNull String contentType, @NonNull String externalId, @NonNull Site site) {
    ImmutableList<String> none = ImmutableList.of();

    SearchServiceResult result = searchService.search(
            /* query */              "",
            /* limit */              -1,
            /* sortCriteria */       none,
            /* folder */             site.getSiteRootFolder(), true,
            /* contentTypes */       contentTypes(contentType), true,
            /* filterQueries */      ImmutableList.of("commerceitems:\"" + externalId + '"', "isdeleted:false"),
            /* facetFieldCriteria */ none,
            /* facetQueries */       none
    );

    return result.getHits();
  }

  @NonNull
  private ImmutableList<ContentType> contentTypes(@NonNull String contentType) {
    ContentType ct = contentRepository.getContentType(contentType);
    return ct != null ? ImmutableList.of(ct) : ImmutableList.of();
  }

  public void setCacheForInSeconds(long cacheForInSeconds) {
    this.cacheForInSeconds = cacheForInSeconds;
  }

  private class SolrQueryCacheKey extends CacheKey<List<Content>> {

    private static final String CACHE_CLASS = "com.coremedia.livecontext.studio.asset.StudioAssetSearchService.SolrQueryCacheKey";

    private final long cacheForInSeconds;
    private final String contentType;
    private final String externalId;
    private final Site site;

    // redundant, only for efficiency
    private final String myEqualsValue;

    SolrQueryCacheKey(String contentType, String externalId, Site site, long cacheForInSeconds) {
      this.contentType = contentType;
      this.externalId = externalId;
      this.site = site;
      this.cacheForInSeconds = cacheForInSeconds;
      myEqualsValue = contentType + externalId + site.getId() + cacheForInSeconds;
    }

    @Override
    public String cacheClass(Cache cache, List<Content> value) {
      return CACHE_CLASS;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      SolrQueryCacheKey that = (SolrQueryCacheKey) o;
      return myEqualsValue.equals(that.myEqualsValue);
    }

    @Override
    public int hashCode() {
      return 29 + myEqualsValue.hashCode();
    }

    @Override
    public List<Content> evaluate(Cache cache) {
      List<Content> result;

      try {
        Cache.disableDependencies();
        result = doSearch(contentType, externalId, site);
      } finally {
        Cache.enableDependencies();
      }

      if (cacheForInSeconds > 0) {
        LOG.debug("Caching for {} s", cacheForInSeconds);
        Cache.cacheFor(cacheForInSeconds, TimeUnit.SECONDS);
      } else {
        LOG.warn("Asset query has unreasonable cache time: {} and will not be cached", cacheForInSeconds);
        Cache.uncacheable();
      }

      return result;
    }
  }
}
