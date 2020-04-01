package com.coremedia.blueprint.analytics.elastic.tasks;

import com.coremedia.blueprint.base.analytics.elastic.PageViewReportModelService;
import com.coremedia.blueprint.base.analytics.elastic.PageViewTaskReportModelService;
import com.coremedia.blueprint.base.analytics.elastic.ReportModel;
import com.coremedia.blueprint.analytics.elastic.retrieval.AnalyticsServiceProvider;
import com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil;
import com.coremedia.blueprint.base.analytics.elastic.validation.ResultItemValidationService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static com.coremedia.blueprint.analytics.elastic.tasks.FetchPublicationsHistoryTask.PUBLICATION_HISTORY_DOCUMENT_TYPE;
import static com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil.needsUpdate;
import static com.google.common.base.Predicates.in;
import static com.google.common.collect.ImmutableSet.copyOf;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;

@Named
class FetchPageViewHistoryRootContentProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(FetchPageViewHistoryRootContentProcessor.class);
  private static final String INTERVAL = "pageViewHistoryInterval";

  private final PageViewReportModelService modelService;
  private final PageViewTaskReportModelService taskReportModelService;
  private final ContentRepository contentRepository;
  private final ResultItemValidationService resultItemValidatorService;

  @Inject
  FetchPageViewHistoryRootContentProcessor(PageViewReportModelService modelService,
                                           PageViewTaskReportModelService taskReportModelService,
                                           ContentRepository contentRepository,
                                           ResultItemValidationService resultItemValidatorService) {
    this.modelService = modelService;
    this.contentRepository = contentRepository;
    this.taskReportModelService = taskReportModelService;
    this.resultItemValidatorService = resultItemValidatorService;
  }

  void processRootContent(@NonNull Content root,
                          @NonNull Map<String, Object> serviceProviderSettings,
                          @NonNull AnalyticsServiceProvider analyticsServiceProvider) {
    final String serviceKey = analyticsServiceProvider.getServiceKey();
    LOG.trace("Processing analytics provider {} for root navigation content {}", serviceKey, root);
    int interval = RetrievalUtil.getInterval(serviceProviderSettings, INTERVAL);
    if (interval <= 0) {
      LOG.debug("Retrieval for content ({}, {}) is disabled. Set setting '{}' greater than 0 to enable retrieval",
              root, serviceKey, interval);
      return;
    }
    ReportModel taskModelForRoot = taskReportModelService.getReportModel(root, serviceKey);
    Date now = new Date();

    if (!serviceProviderSettings.equals(taskModelForRoot.getSettings()) || needsUpdate(taskModelForRoot.getLastSaved(), now.getTime(), interval)) {
      Map<String, Map<String, Long>> data = getValidatedPageViews(analyticsServiceProvider, root, serviceProviderSettings);
      LOG.info("Updating {} page views for ('{}' / '{}')", data.size(), root, analyticsServiceProvider.getServiceKey());
      final Collection<ReportModel> reportModels = Maps.transformEntries(data, new ReportModelUpdater(now, serviceKey)).values();
      modelService.saveAll(reportModels);
      taskModelForRoot.setSettings(serviceProviderSettings);
      savedAt(taskModelForRoot, now);
      taskModelForRoot.setReportData(Lists.newLinkedList(data.keySet()));
      taskModelForRoot.save();
    } else {
      LOG.debug("Report data {} for content ({}, {}) is still fresh", taskModelForRoot, root, serviceKey);
    }
  }

  private Map<String, Map<String, Long>> getValidatedPageViews(AnalyticsServiceProvider analyticsServiceProvider, Content root, Map<String, Object> serviceProviderSettings) {
    try {
      Map<String, Map<String, Long>> data = analyticsServiceProvider.fetchPageViews(root, serviceProviderSettings);
      final Map<String, Map<String, Long>> validatedPageViews = validatePageViews(data);
      LOG.info("Got {} valid page views for ('{}' / '{}') from {} raw entries", validatedPageViews.size(), root, analyticsServiceProvider.getServiceKey(), data.size());
      return validatedPageViews;
    } catch (Exception e) {
      LOG.warn(format("Could not fetch analytics data of provider %s: %s", analyticsServiceProvider.getServiceKey(), e.getMessage()), e);
      return emptyMap();
    }
  }

  private Map<String, Map<String, Long>> validatePageViews(Map<String, Map<String, Long>> data) {
    Iterable<String> validContentIds = resultItemValidatorService.filterValidResultItems(data.keySet(), PUBLICATION_HISTORY_DOCUMENT_TYPE);
    return Maps.filterKeys(data, in(copyOf(validContentIds)));
  }

  void savedAt(ReportModel reportModel, Date now) {
    // this property is used for ttl feature
    reportModel.setLastSavedDate(now);
    reportModel.setLastSaved(now.getTime());
  }

  private class ReportModelUpdater implements Maps.EntryTransformer<String, Map<String, Long>, ReportModel> {
    private final String serviceKey;
    private final Date now;

    public ReportModelUpdater(@NonNull Date now, @NonNull String serviceKey) {
      this.now = now;
      this.serviceKey = serviceKey;
    }

    @Override
    public ReportModel transformEntry(String key, Map<String, Long> value) {
      // id has passed validation already
      Content target = contentRepository.getContent(key);
      ReportModel reportModel = modelService.getReportModel(target, serviceKey);
      reportModel.setReportMap(value);
      savedAt(reportModel, now);

      return reportModel;
    }
  }
}
