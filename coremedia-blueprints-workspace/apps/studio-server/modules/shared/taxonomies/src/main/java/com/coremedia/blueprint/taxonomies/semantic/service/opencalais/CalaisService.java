package com.coremedia.blueprint.taxonomies.semantic.service.opencalais;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.taxonomies.semantic.service.AbstractSemanticService;
import com.coremedia.blueprint.taxonomies.semantic.ContentSerializer;
import com.coremedia.blueprint.taxonomies.semantic.SemanticContext;
import com.coremedia.blueprint.taxonomies.semantic.SemanticEntity;
import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.String.format;

public class CalaisService extends AbstractSemanticService {

  private static final Logger LOG = LoggerFactory.getLogger(CalaisService.class);

  private static final String SERVICE_ID = "open.calais";

  private String apiKey;
  private String apiUrl;

  private CalaisClient client;
  private static final String DDEFALUT_API_URL = "https://api.thomsonreuters.com/permid/calais";

  private SettingsService settingsService;
  private SitesService sitesService;


  @PostConstruct
  @Override
  public void initialize() {
    client = new CalaisClient();
  }

  /**
   * Analyzes the content by sending containing text to OpenCalais and matching it with the internal CoreMedia taxonomies.
   * @param content The content to analyze
   * @return A sematinc context containing the analysis result.
   */
  @Override
  public SemanticContext analyze(Content content) {
    // set API key and URL (if overwritten) with properties from content settings
    configureClient(client, content);

    Multimap<String, SemanticEntity> result = ArrayListMultimap.create();

    CalaisCacheKey calaisCacheKey = new CalaisCacheKey(client, content, getDocumentProperties(), SERVICE_ID);
    List<Map<String, Object>> responses = getCache().get(calaisCacheKey);

    if (responses != null && !responses.isEmpty()) {
      for (Map<String, Object> response : responses) {
        // noinspection unchecked
        List<Map<String, Object>> entities = (List<Map<String, Object>>) response.get("entities");
        for (Map<String, Object> entity : entities) {
          Map<String, String> map = Maps.newHashMap();
          for (String semanticProperty : getSemanticProperties().keySet()) {
            String key = getSemanticProperties().get(semanticProperty);
            map.put(semanticProperty, String.valueOf(entity.get(key)));
          }
          result.put((String) entity.get(getGroupingKey()), SemanticEntity.populate(map));
        }
      }
    }

    return new SemanticContext(SERVICE_ID, result);
  }

  /**
   * Configures the {@link CalaisClient} by determining if OpenCalais config is overwritten
   * in content settings.
   * @param client The client to configure.
   * @param content The content belonging to a site and potentially bringing diferent OpenCalais config.
   */
  private void configureClient(CalaisClient client, Content content) {
    String apiUrlToUse = apiUrl;
    String apiKeyToUse = apiKey;

    Site currentSite = sitesService.getContentSiteAspect(content).getSite();
    Map openCalaisSettings = settingsService.mergedSettingAsMap("openCalais", String.class, String.class, content, currentSite);
    if (openCalaisSettings.containsKey("apiUrl")) {
      LOG.debug("Found OpenCalais apiUrl to use in content settings. Preconfigured property will be replaced.");
      Object oApiUrl = openCalaisSettings.get("apiUrl");
      apiUrlToUse = oApiUrl != null ? String.valueOf(oApiUrl) : DDEFALUT_API_URL;
    }
    if (openCalaisSettings.containsKey("apiKey")) {
      LOG.debug("Found OpenCalais apiKey to use in content settings. Preconfigured property will be replaced.");
      Object oApiKey = openCalaisSettings.get("apiKey");
      apiKeyToUse = String.valueOf(oApiKey);
    }
    client.setApiUrl(apiUrlToUse);
    client.setUniqueAccessKey(apiKeyToUse);
  }

  @Required
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @Override
  public String getApiKey() {
    return apiKey;
  }

  @Override
  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  @SuppressWarnings("unused")
  public String getApiUrl() {
    return apiUrl;
  }

  public void setApiUrl(String apiUrl) {
    this.apiUrl = apiUrl;
  }
}

class CalaisCacheKey extends CacheKey<List<Map<String, Object>>> {
  private static final Logger LOG = LoggerFactory.getLogger(CalaisCacheKey.class);
  private CalaisClient client;
  private List<Map<String, Object>> lastSuccess;
  private String serviceId;
  private Content content;
  private List<String> documentProperties;

  //executor service for the OpenCalais Request: only 4 allowed in the free version
  private ExecutorService service = Executors.newFixedThreadPool(4); //NOSONAR

  CalaisCacheKey(CalaisClient client, Content content, List<String> documentProperties, String serviceId) {
    this.client = client;
    this.serviceId = serviceId;
    this.content = content;
    this.documentProperties = documentProperties;
  }

  @Override
  public List<Map<String, Object>> evaluate(Cache cache) {
    List<Map<String, Object>> responses = new ArrayList<Map<String, Object>>();
    try {
      String extract = ContentSerializer.serialize(content, documentProperties);
      List<String> tokenized = ContentSerializer.tokenizeContent(extract, 10000); //NOSONAR
      List<Future<Map<String, Object>>> futures = new ArrayList<Future<Map<String, Object>>>();

      //trigger callables for the executor service
      for (String part : tokenized) {
        CalaisServiceCallable callable = new CalaisServiceCallable(part, client);
        futures.add(service.submit(callable));
      }

      //...and collect them
      for (Future<Map<String, Object>> entry : futures) {
        try {
          Map<String, Object> callableResult = entry.get(20, TimeUnit.SECONDS); //NOSONAR //timeout of n seconds
          if (callableResult != null) {
            responses.add(callableResult);
          }
        } catch (TimeoutException e) {
          LOG.error("Timeout during waiting for callable " + entry + ": " + e.getMessage(), e);
        }
      }

      // remember last successful response
      if (!responses.isEmpty()) {
        lastSuccess = responses;
      }
    } catch (Exception e) {
      LOG.debug(format("Could not retrieve semantic info from %s", serviceId), e);
      // make sure to return last successful result
      responses = lastSuccess;
    }

    return (responses == null || responses.isEmpty()) ? lastSuccess : responses;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CalaisCacheKey that = (CalaisCacheKey) o;

    return !(client != null ? !client.equals(that.client) : that.client != null)
            && !(content != null ? !content.equals(that.content) : that.content != null)
            && !(serviceId != null ? !serviceId.equals(that.serviceId) : that.serviceId != null);

  }

  @Override
  public int hashCode() {
    int result = client != null ? client.hashCode() : 0;
    result = 31 * result + (serviceId != null ? serviceId.hashCode() : 0);
    result = 31 * result + (content != null ? content.hashCode() : 0);
    result = 31 * result + (documentProperties != null ? documentProperties.hashCode() : 0);
    return result;
  }
}
