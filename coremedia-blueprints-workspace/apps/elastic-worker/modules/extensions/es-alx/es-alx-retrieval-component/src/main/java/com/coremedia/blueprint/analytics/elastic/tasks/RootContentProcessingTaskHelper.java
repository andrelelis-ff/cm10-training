package com.coremedia.blueprint.analytics.elastic.tasks;

import com.coremedia.blueprint.analytics.elastic.retrieval.AnalyticsServiceProvider;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.base.elastic.tenant.TenantSiteMapping;
import com.coremedia.cap.content.Content;
import com.coremedia.elastic.core.api.tenant.TenantService;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.currentTimeMillis;

/**
 * Generic task for fetching data from all configured Analytics providers.
 */
@Named
public class RootContentProcessingTaskHelper {
  private static final Logger LOG = LoggerFactory.getLogger(RootContentProcessingTaskHelper.class);

  private final TenantSiteMapping tenantSiteMapping;
  private final TenantService tenantService;
  private final SettingsService settingsService;
  private final List<AnalyticsServiceProvider> analyticsServiceProviders;

  @Inject
  RootContentProcessingTaskHelper(TenantSiteMapping tenantSiteMapping,
                                  TenantService tenantService,
                                  SettingsService settingsService,
                                  List<AnalyticsServiceProvider> analyticsServiceProviders) {
    this.tenantService = tenantService;
    this.tenantSiteMapping = tenantSiteMapping;
    this.settingsService = settingsService;
    this.analyticsServiceProviders = ImmutableList.copyOf(analyticsServiceProviders);
  }

  /**
   * Collect root navigations for the current tenant to pass them to rootContentProcessingTask
   * @param rootContentProcessingTask the task to process the current tenant's root navigations
   */
  void collectRootNavigationsForTask(AbstractRootContentProcessingTask rootContentProcessingTask) {
    final String currentTenant = tenantService.getCurrent();
    LOG.trace("Starting task for tenant '{}'", currentTenant);
    long start = currentTimeMillis();
    Collection<Content> rootNavigations = tenantSiteMapping.getTenantSiteMap().get(currentTenant);
    if(rootNavigations != null) { // maybe there is no root content available for the current tenant
      processRootNavigations(rootContentProcessingTask, rootNavigations);
      if(LOG.isTraceEnabled()) {
        LOG.trace("Needed {} ms to fetch content visits for tenant {}", (currentTimeMillis() - start), currentTenant);
      }
    }
  }

  /**
   * Check the provider specific settings of the root navigations and delegate to rootContentProcessingTask if the settings aren't
   * null or empty.
   * @param rootContentProcessingTask the task to process the analytics service providers
   * @param rootNavigations the current tenant's root navigations
   */
  private void processRootNavigations(AbstractRootContentProcessingTask rootContentProcessingTask, Collection<Content> rootNavigations) {
    for (Content rootNavigation : rootNavigations) {
      for (AnalyticsServiceProvider analyticsServiceProvider : analyticsServiceProviders) {
        final String serviceKey = analyticsServiceProvider.getServiceKey();
        final Map<String, Object> settings = settingsService.mergedSettingAsMap(serviceKey, String.class, Object.class, rootNavigation);

        if(settings.isEmpty()) {
          LOG.trace("Analytics provider {} not configured for root navigation content {}", serviceKey, rootNavigation);
        } else {
          rootContentProcessingTask.processRootNavigation(rootNavigation, settings, analyticsServiceProvider);
        }
      }
    }
  }

  @PostConstruct
  void initialize() {
    if (LOG.isInfoEnabled()) {
      Map<String, Object> analyticsServiceProvidersByName = new HashMap<>(analyticsServiceProviders.size());
      for (AnalyticsServiceProvider asp : analyticsServiceProviders) {
        analyticsServiceProvidersByName.put(asp.getServiceKey(), asp);
      }
      LOG.info("found analytics service providers {}", analyticsServiceProvidersByName.keySet());
    }
  }

}
