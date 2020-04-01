package com.coremedia.livecontext.ecommerce.ibm.common;

import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.coremedia.cache.EvaluationException;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

class WcLanguageMappingService {

  private static final Logger LOG = LoggerFactory.getLogger(lookup().lookupClass());

  //default is english
  private static final String DEFAULT_LANGUAGE_ID = "-1";

  private static final WcRestServiceMethod<Map, Void> GET_LANGUAGE_MAPPING = WcRestServiceMethod
          .builder(HttpMethod.GET, "coremedia/languagemap", Void.class, Map.class)
          .build();

  private WcRestConnector restConnector;
  private Cache cache;

  private final CacheKey<Map<String, String>> languageMappingCacheKey = new LanguageMappingCacheKey(this);

  private int delayOnErrorSeconds;

  Map<String, String> getLanguageMapping() {
    try {
      return cache.get(languageMappingCacheKey);
    } catch (EvaluationException e) {
      LOG.info("ignoring exception while retrieving language mapping: {}", e.getCause().getMessage());
      return emptyMap();
    }
  }

  Map<String, String> getLanguageMappingUncached() {
    //noinspection unchecked
    return restConnector.callServiceInternal(GET_LANGUAGE_MAPPING, emptyList(), emptyMap(), null, null, null)
            .orElse(null);
  }

  /**
   * Gets IBM specific language Id for a given locale String.
   * If a certain mapping does not exist or locale String is invalid, the default "-1" for "en" is returned.
   *
   * @param locale e.g. "en_US" "en" "de"
   */
  @NonNull
  String getLanguageId(@Nullable Locale locale) {
    return findLanguageId(locale).orElse(DEFAULT_LANGUAGE_ID);
  }

  @NonNull
  private Optional<String> findLanguageId(@Nullable Locale locale) {
    if (locale == null) {
      return Optional.empty();
    }

    Map mapping = getLanguageMapping();
    if (mapping == null) {
      return Optional.empty();
    }

    String localeStr = locale.toString();
    String key = mapping.containsKey(localeStr) ? localeStr : locale.getLanguage();

    String langId = (String) mapping.get(key);
    return Optional.ofNullable(langId);
  }

  int getDelayOnErrorSeconds() {
    return delayOnErrorSeconds;
  }

  @Autowired
  public void setCache(Cache cache) {
    this.cache = cache;
  }

  @Autowired
  public void setRestConnector(WcRestConnector connector) {
    this.restConnector = connector;
  }

  @Value("${livecontext.ibm.languageMapping.delayOnErrorSeconds:30}")
  public void setDelayOnErrorSeconds(int delayOnErrorSeconds) {
    this.delayOnErrorSeconds = delayOnErrorSeconds;
  }
}
