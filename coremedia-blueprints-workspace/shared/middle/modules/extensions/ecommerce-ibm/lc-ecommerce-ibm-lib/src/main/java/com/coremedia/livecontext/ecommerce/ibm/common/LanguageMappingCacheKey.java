package com.coremedia.livecontext.ecommerce.ibm.common;

import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodHandles.lookup;

class LanguageMappingCacheKey extends CacheKey<Map<String, String>> {

  private static final Logger LOG = LoggerFactory.getLogger(lookup().lookupClass());

  @NonNull
  private final WcLanguageMappingService wcLanguageMappingService;

  LanguageMappingCacheKey(@NonNull WcLanguageMappingService wcLanguageMappingService) {
    this.wcLanguageMappingService = wcLanguageMappingService;
  }

  @Override
  public Map<String, String> evaluate(Cache cache) throws Exception {
    Map<String, String> languageMapping = wcLanguageMappingService.getLanguageMappingUncached();
    if (languageMapping == null || languageMapping.isEmpty()) {
      int delayOnErrorSeconds = wcLanguageMappingService.getDelayOnErrorSeconds();
      LOG.warn("Could not determine the value of Language Mapping! Falling back to default behavior. Retry in {} seconds", delayOnErrorSeconds);
      Cache.cacheFor(delayOnErrorSeconds, TimeUnit.SECONDS);
    } else {
      LOG.debug("Got language mapping '{}'.", languageMapping);
    }
    return languageMapping;
  }

  @Override
  public String cacheClass(Cache cache, Map<String, String> value) {
    return Cache.CACHE_CLASS_ALWAYS_STAY_IN_CACHE;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    LanguageMappingCacheKey that = (LanguageMappingCacheKey) o;

    return wcLanguageMappingService.equals(that.wcLanguageMappingService);
  }

  @Override
  public int hashCode() {
    return wcLanguageMappingService.hashCode();
  }
}
