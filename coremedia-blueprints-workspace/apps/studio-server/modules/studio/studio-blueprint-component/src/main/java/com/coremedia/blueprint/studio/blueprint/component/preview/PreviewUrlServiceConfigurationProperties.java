package com.coremedia.blueprint.studio.blueprint.component.preview;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "studio.previewurlservice")
public class PreviewUrlServiceConfigurationProperties {

  private boolean enabled = false;
  private Map<String, String> config = new HashMap<>();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Map<String, String> getConfig() {
    return config;
  }

  public void setConfig(Map<String, String> config) {
    this.config = config;
  }

}
