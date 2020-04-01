package com.coremedia.blueprint.analytics.elastic.tasks;

import com.coremedia.blueprint.base.analytics.elastic.util.DateUtil;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.Version;
import com.coremedia.cap.content.publication.PublicationService;
import com.coremedia.cap.content.query.QueryService;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PublicationsAggregator {

  private static final Logger LOG = LoggerFactory.getLogger(PublicationsAggregator.class);
  static final String QUERY_SERVICE_EXPRESSION_TEMPLATE = "TYPE %s: versionPublicationDate > ?0 AND BELOW ?1";

  private final PublicationService publicationService;
  private final QueryService queryService;
  private final Content rootNavigation;
  private final Calendar startTime;
  private final String documentType;
  private final SitesService sitesService;

  public PublicationsAggregator(ContentRepository repository,
                                SitesService sitesService,
                                Content rootNavigation,
                                Calendar startTime,
                                String documentType) {
    this.sitesService = sitesService;
    this.publicationService = repository.getPublicationService();
    this.queryService = repository.getQueryService();
    this.rootNavigation = rootNavigation;
    this.startTime = startTime;
    this.documentType = documentType;
  }

  Map<String, Long> aggregatePublications() {
    long start = System.currentTimeMillis();
    Site site = sitesService.getContentSiteAspect(rootNavigation).getSite();
    if (site == null) {
      LOG.warn(String.format("Cannot determine site for root navigation %s, skip it", rootNavigation));
      return Collections.emptyMap();
    }

    final Content siteFolder = site.getSiteRootFolder();
    LOG.info("aggregating publications of contents with type {} below {} ...", documentType, siteFolder.getPath());
    Collection<Version> versions = queryService.poseVersionQuery(
            String.format(QUERY_SERVICE_EXPRESSION_TEMPLATE, documentType),
            startTime,
            siteFolder);
    final Map<String, Long> stringLongMap = aggregatePublications(versions);
    if (LOG.isInfoEnabled()) {
      LOG.info("It took {}ms to calculate publications statistics for {} published versions" +
              " after {} for site {} (path {}) with root folder {}",
              System.currentTimeMillis() - start,
                      versions.size(),
                      startTime.getTime(),
                      rootNavigation,
                      rootNavigation.getPath(),
                      siteFolder.getPath());
    }
    return stringLongMap;
  }

  private Map<String, Long> aggregatePublications(Collection<Version> versions) {
    Map<String, Long> aggregatedPublications = new HashMap<>();

    for (Version version : versions) {
      Calendar publicationOfVersion = publicationService.getPublicationDate(version);
      if (publicationOfVersion != null) {
        String dateString = DateUtil.getReportDateString(publicationOfVersion);
        Long latestCount = aggregatedPublications.get(dateString);
        if (latestCount != null) {
          aggregatedPublications.put(dateString, ++latestCount);
        } else {
          aggregatedPublications.put(dateString, 1L);
        }
      }
    }
    return aggregatedPublications;
  }

}
