package com.coremedia.blueprint.cae.sitemap;

/**
 * Callback interface for URL generators
 */
public interface UrlCollector {
  /**
   * Append another URL to the list
   *
   * @param url the URL
   */
  void appendUrl(String url);
}
