package com.coremedia.livecontext.ecommerce.ibm.storeinfo;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractCommerceCacheKey.CONFIG_KEY_STORE_INFO;
import static com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractCommerceCacheKey.INVALIDATE_ALL_EVENT;

@DefaultAnnotation(NonNull.class)
public class StoreInfoCacheKey extends CacheKey<Map<String, Object>> {

  private static final Logger LOG = LoggerFactory.getLogger(StoreInfoCacheKey.class);

  private final WcStoreInfoWrapperService wrapperService;
  private final long cacheDurationInSeconds;
  private final int delayOnError;

  StoreInfoCacheKey(WcStoreInfoWrapperService wrapperService, CommerceCache commerceCache, int delayOnError) {
    this.wrapperService = wrapperService;
    this.delayOnError = delayOnError;

    cacheDurationInSeconds = commerceCache.getCacheDurationInSeconds(CONFIG_KEY_STORE_INFO);
  }

  @Override
  public Map<String, Object> evaluate(Cache cache) {
    try {
      Cache.dependencyOn(INVALIDATE_ALL_EVENT);
      Cache.disableDependencies();

      Map<String, Object> storeInfos = wrapperService.getStoreInfos();

      // turn dependencies on again
      Cache.enableDependencies();
      if (storeInfos.isEmpty()) {
        LOG.warn("no store info provided by WCS, retrying in {} seconds", delayOnError);
        Cache.cacheFor(delayOnError, TimeUnit.SECONDS);
      } else {
        LOG.info("caching store infos provided by WCS for {} seconds: '{}'", cacheDurationInSeconds, storeInfos);
        Cache.cacheFor(cacheDurationInSeconds, TimeUnit.SECONDS);
      }

      return storeInfos;
    } finally {
      // turn dependencies on in case of an exception
      if (!Cache.dependenciesAreOn()) {
        Cache.enableDependencies();
      }
      Cache.dependencyOn(CONFIG_KEY_STORE_INFO);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    StoreInfoCacheKey that = (StoreInfoCacheKey) o;
    return wrapperService.equals(that.wrapperService);
  }

  @Override
  public int hashCode() {
    return Objects.hash(wrapperService);
  }
}
