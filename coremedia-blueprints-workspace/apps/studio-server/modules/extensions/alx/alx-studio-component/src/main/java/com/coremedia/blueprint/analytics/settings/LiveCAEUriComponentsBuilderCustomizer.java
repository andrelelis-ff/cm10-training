package com.coremedia.blueprint.analytics.settings;

import com.coremedia.rest.cap.content.UrlModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

/**
 * Add the configured live CAE settings to a {@link UriComponentsBuilder}.
 */
@Named
public class LiveCAEUriComponentsBuilderCustomizer {

  private static final Logger LOG = LoggerFactory.getLogger(LiveCAEUriComponentsBuilderCustomizer.class);
  private static final int LIVE_CAE_PORT = 49080;

  private final List<UrlModifier> urlModifiers = new ArrayList<>();

  private String liveCaeHost;
  private int liveCaePort = LIVE_CAE_PORT;
  private String liveCaeScheme;

  @Value("${es.cae.http.host:localhost}")
  public void setLiveCaeHost(String liveCaeHost) {
    this.liveCaeHost = liveCaeHost;
  }

  @Value("${es.cae.http.port:" + LIVE_CAE_PORT + "}")
  public void setLiveCaePort(int liveCaePort) {
    this.liveCaePort = liveCaePort;
  }

  @Value("${es.cae.protocol:http}")
  public void setLiveCaeScheme(String liveCaeScheme) {
    this.liveCaeScheme = liveCaeScheme;
  }

  @Autowired(required = false)
  public void setUrlModifiers(List<UrlModifier> urlModifiers) {
    this.urlModifiers.clear();
    this.urlModifiers.addAll(urlModifiers);
  }

  public void fillIn(UriComponentsBuilder uriComponentsBuilder) {
    if(hasText(liveCaeHost)) {
      uriComponentsBuilder.host(getModified(liveCaeHost));
    }
    if(hasText(liveCaeScheme)) {
      uriComponentsBuilder.scheme(liveCaeScheme);
    }
    uriComponentsBuilder.port(liveCaePort);
  }

  private String getModified(String urlFragment) {
    for(UrlModifier urlModifier : urlModifiers) {
      // check if one of the urlModifiers modifies the given url fragment (giving us a tenant)
      final String modified = urlModifier.processUrl(urlFragment);
      if(!urlFragment.equals(modified)) {
        LOG.debug("modified url fragment {} to {}", urlFragment, modified);
        return modified;
      }
    }
    return urlFragment;
  }

  @PostConstruct
  void logConfiguration() {
    LOG.info("customizing deep link url using {}", this);
  }

  @Override
  public String toString() {
    return "LiveCAEUriComponentsBuilderCustomizer{" +
            "urlModifiers=" + urlModifiers +
            ", liveCaeHost='" + liveCaeHost + '\'' +
            ", liveCaePort='" + liveCaePort + '\'' +
            ", liveCaeScheme='" + liveCaeScheme + '\'' +
            '}';
  }
}
