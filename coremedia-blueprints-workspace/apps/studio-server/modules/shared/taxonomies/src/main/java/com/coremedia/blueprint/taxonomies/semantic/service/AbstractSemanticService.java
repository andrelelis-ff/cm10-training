package com.coremedia.blueprint.taxonomies.semantic.service;

import com.coremedia.blueprint.taxonomies.semantic.SemanticContext;
import com.coremedia.blueprint.taxonomies.semantic.SemanticEntity;
import com.coremedia.cache.Cache;
import com.coremedia.cap.content.Content;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

public abstract class AbstractSemanticService implements SemanticService {

  private String apiKey;
  private List<String> documentProperties;
  private Map<String, String> semanticProperties;
  private Cache cache;
  private String groupingKey = SemanticEntity.TYPE;

  @PostConstruct
  public abstract void initialize();

  @Override
  public abstract SemanticContext analyze(Content content);

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public List<String> getDocumentProperties() {
    return documentProperties;
  }

  public void setDocumentProperties(List<String> documentProperties) {
    this.documentProperties = documentProperties;
  }

  public Map<String, String> getSemanticProperties() {
    return semanticProperties;
  }

  public void setSemanticProperties(Map<String, String> semanticProperties) {
    this.semanticProperties = semanticProperties;
  }

  public void setGroupingKey(String groupingKey) {
    this.groupingKey = groupingKey;
  }

  public void setCache(Cache cache) {
    this.cache = cache;
  }

  public Cache getCache() {
    return cache;
  }

  public String getGroupingKey() {
    return groupingKey;
  }
}
