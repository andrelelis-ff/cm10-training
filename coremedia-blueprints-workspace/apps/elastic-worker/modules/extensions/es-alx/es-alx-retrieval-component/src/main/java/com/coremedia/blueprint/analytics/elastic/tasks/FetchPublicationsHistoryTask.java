package com.coremedia.blueprint.analytics.elastic.tasks;

import com.coremedia.blueprint.base.analytics.elastic.PublicationReportModelService;
import com.coremedia.blueprint.base.analytics.elastic.ReportModel;
import com.coremedia.blueprint.base.analytics.elastic.util.DaysBack;
import com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.base.elastic.tenant.TenantSiteMapping;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static com.coremedia.blueprint.base.analytics.elastic.util.DateUtil.getDateWithoutTime;
import static com.google.common.collect.Maps.newHashMap;

/**
 * A task to aggregate all publications below root channel paths.
 */
@Named
public class FetchPublicationsHistoryTask implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(FetchPublicationsHistoryTask.class);

  static final int DEFAULT_TIME_RANGE = 30;
  static final String PUBLICATION_HISTORY_INTERVAL_KEY = "publicationHistoryInterval";
  static final String PUBLICATION_HISTORY_DOCUMENT_TYPE_KEY = "publicationHistoryDocumentType";
  static final String PUBLICATION_HISTORY_DOCUMENT_TYPE = "CMLinkable";
  static final int PUBLICATION_HISTORY_INTERVAL = 180; // 3 hours

  private final ContentRepository repository;
  private final TenantSiteMapping tenantSiteMapping;
  private final PublicationReportModelService publicationReportModelService;
  private final SitesService sitesService;
  private final SettingsService settingsService;

  @Inject
  public FetchPublicationsHistoryTask(ContentRepository repository,
                                      SitesService sitesService,
                                      TenantSiteMapping tenantSiteMapping,
                                      PublicationReportModelService publicationReportModelService,
                                      SettingsService settingsService) {
    this.repository = repository;
    this.sitesService = sitesService;
    this.tenantSiteMapping = tenantSiteMapping;
    this.publicationReportModelService = publicationReportModelService;
    this.settingsService = settingsService;
  }

  @Override
  public void run() {
    Collection<Content> rootNavigationsList = tenantSiteMapping.getRootsForCurrentTenant();
    for (Content rootNavigation : rootNavigationsList) {
      getPublications(rootNavigation);
    }
  }

  private void getPublications(Content rootNavigation) {
    int interval = settingsService.settingWithDefault(PUBLICATION_HISTORY_INTERVAL_KEY, Integer.class, PUBLICATION_HISTORY_INTERVAL, rootNavigation);
    String documentType = settingsService.settingWithDefault(PUBLICATION_HISTORY_DOCUMENT_TYPE_KEY, String.class, PUBLICATION_HISTORY_DOCUMENT_TYPE, rootNavigation);

    ReportModel publicationReportModel = publicationReportModelService.getReportModel(rootNavigation);
    long lastRun = publicationReportModel.getLastSaved();
    String lastDocumentType = (String) publicationReportModel.getSettings().get(PUBLICATION_HISTORY_DOCUMENT_TYPE_KEY);

    boolean hasChangedSettings = !documentType.equals(lastDocumentType);
    long now = System.currentTimeMillis();
    if (RetrievalUtil.needsUpdate(lastRun, now, interval) || hasChangedSettings) {
      Calendar startTime = getReportStartTime(lastRun, hasChangedSettings);
      PublicationsAggregator publicationsAggregator = new PublicationsAggregator(repository, sitesService, rootNavigation, startTime, documentType);

      Map<String, Long> newPublications = publicationsAggregator.aggregatePublications();

      // make last publications modifiable
      HashMap<String, Long> allPublications = newHashMap(publicationReportModel.getReportMap());
      allPublications.putAll(newPublications);
      publicationReportModel.setReportMap(allPublications);

      Date currentDate = new Date();
      // this property is used for ttl feature
      publicationReportModel.setLastSavedDate(currentDate);
      publicationReportModel.setLastSaved(currentDate.getTime());
      publicationReportModel.setSettings(ImmutableMap.<String, Object>of(PUBLICATION_HISTORY_DOCUMENT_TYPE_KEY, documentType));
      publicationReportModel.save();
    }
  }

  /**
   * Returns a calendar representing the start for the publication history query.
   * This is either the beginning of the day the task was last successfully executed
   * or the beginning of the day 30 days ago.
   *
   * @param lastRun            time of the last successful collectRootNavigationsForTask
   * @param hasChangedSettings TRUE, if the settings changed since the last time we fetched data
   * @return a calendar representing the start for the publication history query
   */
  private Calendar getReportStartTime(long lastRun, boolean hasChangedSettings) {
    long startTime;
    boolean weHaveReportData = lastRun > 0;
    boolean settingsDidNotChange = !hasChangedSettings;
    if (weHaveReportData && settingsDidNotChange) {
      Date lastSavedAsDate = getDateWithoutTime(new Date(lastRun));
      LOG.trace("Use date from model: " + lastSavedAsDate);
      startTime = lastSavedAsDate.getTime();
    } else {
      DaysBack daysBack = new DaysBack(DEFAULT_TIME_RANGE);
      LOG.trace("Use initial date: " + daysBack.getStartDate());
      startTime = getDateWithoutTime(daysBack.getStartDate()).getTime();
    }
    Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
    calendar.setTimeInMillis(startTime);

    return calendar;
  }
}