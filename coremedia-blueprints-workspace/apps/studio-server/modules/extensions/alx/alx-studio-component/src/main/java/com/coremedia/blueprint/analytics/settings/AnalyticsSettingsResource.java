package com.coremedia.blueprint.analytics.settings;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Named
@RequestMapping(value = "alxservice", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class AnalyticsSettingsResource {

  private static final Logger LOG = LoggerFactory.getLogger(AnalyticsSettingsResource.class);

  private final Collection<AnalyticsSettingsProvider> analyticsSettingsProviders;
  private final ContentRepository contentRepository;

  @Autowired
  AnalyticsSettingsResource(ContentRepository contentRepository,
                            Collection<AnalyticsSettingsProvider> analyticsSettingsProviders) {
    this.contentRepository = contentRepository;
    this.analyticsSettingsProviders = Collections.unmodifiableCollection(analyticsSettingsProviders);
  }

  @GetMapping("/{id}")
  public Map<String, String> getAlxUrl(@PathVariable("id") String id) {

    final Content content = contentRepository.getContent(id);

    final Map<String, String> alxUrlMap = new HashMap<>(analyticsSettingsProviders.size());

    if (content != null && !content.isDeleted()) {
      for (AnalyticsSettingsProvider analyticsSettingsProvider : analyticsSettingsProviders) {
        String reportURL = null;
        try {
          reportURL = analyticsSettingsProvider.getReportUrlFor(content);
        } catch (RuntimeException e) {
          LOG.info("ignoring exception while creating report url for service {} and content {}: {}",
                  analyticsSettingsProvider.getServiceKey(),
                  content,
                  e.getMessage());
        }
        alxUrlMap.put(analyticsSettingsProvider.getServiceKey(), reportURL);
      }
    }

    return alxUrlMap;
  }

  @PostConstruct
  void initialize() {
    LOG.info("Found analytics providers {}", analyticsSettingsProviders);
  }

}
