package com.coremedia.blueprint.analytics.elastic.tasks;

import com.coremedia.blueprint.base.analytics.elastic.ReportModel;
import com.coremedia.blueprint.base.analytics.elastic.TopNReportModelService;
import com.coremedia.blueprint.analytics.elastic.retrieval.AnalyticsServiceProvider;
import com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil;
import com.coremedia.blueprint.base.analytics.elastic.util.SettingsUtil;
import com.coremedia.blueprint.base.analytics.elastic.validation.ResultItemValidationService;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.elastic.core.api.tenant.TenantService;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil.KEY_INTERVAL;
import static com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil.needsUpdate;
import static com.coremedia.blueprint.base.analytics.elastic.util.SettingsUtil.createProxy;
import static com.google.common.collect.ImmutableList.copyOf;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.math.NumberUtils.isDigits;

/**
 * The main data retrieval task to fetch reports. This task checks all CMALXBaseList instances of the
 * tenant that owns the report task and fetches all configured reports. Exceptions thrown while retrieving
 * a report are logged and lead to empty report result. Use a reasonable retrieval interval to avoid DoS attacks
 * on your third party analytics provider integration. Default interval is once per day.
 *
 * @see AnalyticsServiceProvider
 * @see CMALXBaseListService
 */
@Named
public class FetchReportsTask extends AbstractRootContentProcessingTask {

  private static final Logger LOG = LoggerFactory.getLogger(FetchReportsTask.class);
  private static final String CONTENT_BEAN_ID_PREFIX = "contentbean:";

  private final CMALXBaseListService cmalxBaseListService;
  private final TopNReportModelService modelService;
  private final TenantService tenantService;
  private final ResultItemValidationService resultItemValidatorService;
  private final SitesService sitesService;

  @Inject
  public FetchReportsTask(CMALXBaseListService cmalxBaseListService,
                          TopNReportModelService modelService,
                          TenantService tenantService,
                          ResultItemValidationService resultItemValidationService,
                          RootContentProcessingTaskHelper rootContentProcessingTaskHelper,
                          SitesService sitesService) {
    super(rootContentProcessingTaskHelper);
    this.cmalxBaseListService = cmalxBaseListService;
    this.modelService = modelService;
    this.tenantService = tenantService;
    this.resultItemValidatorService = resultItemValidationService;
    this.sitesService = sitesService;
  }

  @Override
  void processRootNavigation(@NonNull Content rootNavigation,
                             @NonNull Map<String, Object> serviceSettings,
                             @NonNull AnalyticsServiceProvider analyticsServiceProvider) {

    final String currentTenant = tenantService.getCurrent();
    final List<Content> cmalxBaseLists = cmalxBaseListService.getCMALXBaseLists(rootNavigation, currentTenant);

    LOG.trace("processing cmalxBaseList documents {} for tenant {}", cmalxBaseLists, currentTenant);
    for (Content cmalxBaseList : cmalxBaseLists) {
      Site site = sitesService.getContentSiteAspect(cmalxBaseList).getSite();
      if (null != site && sitesService.isContentInSite(site, rootNavigation)) {
        LOG.trace("Processing cmalxBaseList document {} for tenant {} and site {}", cmalxBaseList, currentTenant, site);
        processAlxBaseList(cmalxBaseList, rootNavigation, analyticsServiceProvider);
      }
    }
  }

  private void processAlxBaseList(Content cmalxBaseList, Content rootNavigation, AnalyticsServiceProvider analyticsServiceProvider) {
    final String serviceKey = analyticsServiceProvider.getServiceKey();

    final String explicitlyConfiguredServiceKey = cmalxBaseList.getString(RetrievalUtil.DOCUMENT_PROPERTY_ANALYTICS_PROVIDER);
    // only fetch data for this list, if the provider is inherited or matches the explicitly configured one
    if(isBlank(explicitlyConfiguredServiceKey) || explicitlyConfiguredServiceKey.equals(serviceKey)) {
      final Map<String, Object> effectiveSettings = analyticsServiceProvider.computeEffectiveRetrievalSettings(cmalxBaseList, rootNavigation);

      if (effectiveSettings == null || effectiveSettings.isEmpty()) {
        LOG.info("No settings found for analytics provider {}", serviceKey);
        return;
      }
      final RetrievalBaseSettings retrievalBaseSettings = createProxy(RetrievalBaseSettings.class, effectiveSettings);

      fetchDataIfNecessary(cmalxBaseList, analyticsServiceProvider, serviceKey, effectiveSettings, retrievalBaseSettings);
    }

  }

  private void fetchDataIfNecessary(Content cmalxBaseList, AnalyticsServiceProvider analyticsServiceProvider, String serviceKey, Map<String, Object> effectiveSettings, RetrievalBaseSettings retrievalBaseSettings) {
    final ReportModel reportModel = modelService.getReportModel(cmalxBaseList, serviceKey);

    if (isRetrievalEnabled(effectiveSettings, retrievalBaseSettings, reportModel)) {

      final long start = System.currentTimeMillis();

      // fetch data only if its too old or if the effective settings have changed
      if (needsUpdate(reportModel.getLastSaved(), start, retrievalBaseSettings.getInterval()) || settingsHaveChanged(effectiveSettings, reportModel)) {
        fetchData(cmalxBaseList, analyticsServiceProvider, effectiveSettings, retrievalBaseSettings, reportModel, start);
      } else {
        LOG.trace("Report data {} for list ({}, {}) is still fresh", reportModel, cmalxBaseList, serviceKey);
      }
    }
  }

  static boolean isRetrievalEnabled(Map<String, Object> effectiveSettings, RetrievalBaseSettings retrievalBaseSettings, ReportModel reportModel) {
    final int interval = retrievalBaseSettings.getInterval();
    if (interval <= 0) {
      if (interval != SettingsUtil.createProxy(RetrievalBaseSettings.class, reportModel.getSettings()).getLimit()) {
        // settings have changed
        reportModel.setSettings(effectiveSettings);
        reportModel.save();
        if (LOG.isInfoEnabled()) {
          LOG.info("Retrieval for list ({}, {}) is disabled. Set setting '{}' greater than 0 to enable retrieval",
                  reportModel.getTarget(), reportModel.getService(), KEY_INTERVAL);
        }
      }
      return false;
    }
    return true;
  }

  private static boolean settingsHaveChanged(@NonNull Map<String, Object> effectiveSettings, ReportModel reportModel) {
    return !effectiveSettings.equals(reportModel.getSettings());
  }

  /**
   * Fetch data for a list and save the report model.
   */
  private void fetchData(final Content cmalxBaseListContent,
                         final AnalyticsServiceProvider analyticsServiceProvider,
                         final Map<String, Object> settings,
                         final RetrievalBaseSettings retrievalBaseSettings,
                         final ReportModel reportModel,
                         final long start) {
    final String serviceKey = analyticsServiceProvider.getServiceKey();

    LOG.info("fetching data for list {} using service provider {}", cmalxBaseListContent, serviceKey);
    final List<String> rawReportData = fetchData(cmalxBaseListContent, analyticsServiceProvider, settings);

    final List<String> postProcessedReportData = optimizeForRendering(rawReportData, cmalxBaseListContent, retrievalBaseSettings);
    reportModel.setReportData(postProcessedReportData);

    final long end = System.currentTimeMillis();
    // this property is used for ttl feature
    reportModel.setLastSavedDate(new Date(end));
    reportModel.setLastSaved(end);
    reportModel.setSettings(settings);
    if (LOG.isInfoEnabled()) {
      LOG.info("Processing of list ({}, {}) took {} millis. Result is {}, filtered from {} raw results.",
              cmalxBaseListContent, serviceKey, end - start, postProcessedReportData, rawReportData.size());
    }
    reportModel.save();
  }

  /**
   * Optimize the given list of raw result items for rendering (e.g. check that numeric values are valid content ids)
   *
   * @param rawResultItems        raw result list fetched from the 3rd party provider
   * @param cmalxBaseListContent  the content proxy of the Top N list
   * @param retrievalBaseSettings the statically typed settings proxy
   * @return list of strings (most likely content bean ids) optimized for rendering
   */
  private List<String> optimizeForRendering(List<String> rawResultItems, Content cmalxBaseListContent, RetrievalBaseSettings retrievalBaseSettings) {
    final int maxLength = retrievalBaseSettings.getMaxLength();
    final List<String> validated = copyOf(resultItemValidatorService.filterValidResultItems(rawResultItems, cmalxBaseListContent, maxLength));
    return Lists.transform(validated, new ContentIdToContentBeanIdTransformer());
  }

  private static String getContentBeanId(String contentId) {
    return CONTENT_BEAN_ID_PREFIX +IdHelper.parseContentId(contentId);
  }

  /**
   * Let the analytics service provider fetch data.
   */
  private List<String> fetchData(Content cmalxBaseListContent,
                                 AnalyticsServiceProvider analyticsServiceProvider,
                                 final Map<String, Object> settings) {
    try {
      return analyticsServiceProvider.fetchDataFor(cmalxBaseListContent, settings);
    } catch (Exception e) {
      LOG.warn("ignoring exception while retrieving data for list ({}, {}): {}", cmalxBaseListContent, analyticsServiceProvider.getServiceKey(), e.getMessage());
      return Collections.emptyList();
    }
  }

  private static class ContentIdToContentBeanIdTransformer implements Function<String, String> {
    @Override
    public String apply(@Nullable String input) {
      return IdHelper.isContentId(input) || isDigits(input) ? getContentBeanId(input) : input;
    }
  }
}