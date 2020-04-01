package com.coremedia.blueprint.studio.rest.externalpreview;

/**
 * Config representation wrapper for the external preview.
 */
public class ExternalPreviewConfigRepresentation {
  private String restUrl;
  private String previewUrl;
  private String urlPrefix;

  public String getRestUrl() {
    return restUrl;
  }

  public String getPreviewUrl() {
    return previewUrl;
  }

  public String getUrlPrefix() {
    return urlPrefix;
  }

  public void setRestUrl(String restUrl) {
    this.restUrl = restUrl;
  }

  public void setPreviewUrl(String previewUrl) {
    this.previewUrl = previewUrl;
  }

  public void setUrlPrefix(String urlPrefix) {
    this.urlPrefix = urlPrefix;
  }
}
