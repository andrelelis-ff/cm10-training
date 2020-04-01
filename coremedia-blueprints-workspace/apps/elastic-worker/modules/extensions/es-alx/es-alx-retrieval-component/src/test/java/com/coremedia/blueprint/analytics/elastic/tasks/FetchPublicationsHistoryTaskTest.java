package com.coremedia.blueprint.analytics.elastic.tasks;

import com.coremedia.blueprint.base.analytics.elastic.PublicationReportModelService;
import com.coremedia.blueprint.base.analytics.elastic.ReportModel;
import com.coremedia.blueprint.base.analytics.elastic.util.DaysBack;
import com.coremedia.blueprint.base.elastic.tenant.TenantSiteMapping;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.Version;
import com.coremedia.cap.content.publication.PublicationService;
import com.coremedia.cap.content.query.QueryService;
import com.coremedia.cap.multisite.ContentSiteAspect;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.lang.reflect.Proxy;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static com.coremedia.blueprint.analytics.elastic.tasks.FetchPublicationsHistoryTask.PUBLICATION_HISTORY_DOCUMENT_TYPE;
import static com.coremedia.blueprint.analytics.elastic.tasks.FetchPublicationsHistoryTask.PUBLICATION_HISTORY_DOCUMENT_TYPE_KEY;
import static com.coremedia.blueprint.analytics.elastic.tasks.FetchPublicationsHistoryTask.PUBLICATION_HISTORY_INTERVAL;
import static com.coremedia.blueprint.analytics.elastic.tasks.FetchPublicationsHistoryTask.PUBLICATION_HISTORY_INTERVAL_KEY;
import static com.coremedia.blueprint.analytics.elastic.tasks.PublicationsAggregator.QUERY_SERVICE_EXPRESSION_TEMPLATE;
import static com.coremedia.blueprint.base.analytics.elastic.ReportModel.REPORT_DATE_FORMAT;
import static com.coremedia.blueprint.base.analytics.elastic.util.DateUtil.getDateWithoutTime;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = FetchPublicationsHistoryTaskTest.LocalConfig.class)
public class FetchPublicationsHistoryTaskTest {

  private Date referenceDate;
  private static final String REFERENCE_DATE_STRING = "20130601";
  private Map<String, Long> assertReportMap = new HashMap<>();
  private FetchPublicationsHistoryTask fetchPublicationsHistory;

  @Inject
  private ContentRepository contentRepository;

  @Mock
  private QueryService queryService;

  @Mock
  private PublicationService publicationService;

  @Mock
  private ReportModel reportModel;

  @Mock
  private PublicationReportModelService publicationReportModelService;

  @Mock
  private Map<String, Long> reportMap;

  @Mock
  private SitesService sitesService;

  @Mock
  private Site site;

  @Mock
  private ContentSiteAspect contentSiteAspect;

  @Mock
  private Content siteFolder;

  @Mock
  private SettingsService settingsService;

  @Mock
  private Version version1;

  @Mock
  private Version version2;

  @Before
  public void setup() throws ParseException {
    MockitoAnnotations.initMocks(this);

    referenceDate = new SimpleDateFormat(REPORT_DATE_FORMAT).parse(REFERENCE_DATE_STRING);

    initQueryService();
    final ContentRepository repositoryProxy = createContentRepositoryProxy(contentRepository, publicationService);
    final TenantSiteMapping tenantSiteMapping = mock(TenantSiteMapping.class);
    when(tenantSiteMapping.getRootsForCurrentTenant()).thenReturn(Collections.singleton(contentRepository.getContent(IdHelper.formatContentId(12346))));

    fetchPublicationsHistory = new FetchPublicationsHistoryTask(repositoryProxy, sitesService, tenantSiteMapping, publicationReportModelService, settingsService);

    when(publicationReportModelService.getReportModel(any(Content.class))).thenReturn(reportModel);
    when(reportModel.getLastSaved()).thenReturn(referenceDate.getTime());

    when(reportModel.getReportMap()).thenReturn(reportMap);

    when(settingsService.settingWithDefault(eq(PUBLICATION_HISTORY_DOCUMENT_TYPE_KEY), eq(String.class), eq(PUBLICATION_HISTORY_DOCUMENT_TYPE), any(Content.class))).thenReturn(PUBLICATION_HISTORY_DOCUMENT_TYPE);
    when(settingsService.settingWithDefault(eq(PUBLICATION_HISTORY_INTERVAL_KEY), eq(Integer.class), eq(PUBLICATION_HISTORY_INTERVAL), any(Content.class))).thenReturn(PUBLICATION_HISTORY_INTERVAL);

    when(sitesService.getContentSiteAspect(any(Content.class))).thenReturn(contentSiteAspect);
    when(contentSiteAspect.getSite()).thenReturn(site);
    when(site.getSiteRootFolder()).thenReturn(siteFolder);
  }

  private void initQueryService() {
    when(queryService.poseVersionQuery(anyString(), any())).thenReturn(Arrays.asList(version1,version2));
  }

  private ContentRepository createContentRepositoryProxy(ContentRepository contentRepository, PublicationService publicationService1) {
    return (ContentRepository) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{ContentRepository.class},
            (proxy, method, args) -> {
              String methodName = method.getName();
              switch (methodName) {
                case "getQueryService":
                  return queryService;
                case "getPublicationService":
                  return publicationService1;
                default:
                  return method.invoke(contentRepository, args);
              }
            });
  }

  @Test
  public void getPublicationsTest() {
    when(reportModel.getSettings()).thenReturn(ImmutableMap.of(PUBLICATION_HISTORY_DOCUMENT_TYPE_KEY, PUBLICATION_HISTORY_DOCUMENT_TYPE));
    Calendar startTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    startTime.setTime(referenceDate);

    fetchPublicationsHistory.run();

    verify(queryService).poseVersionQuery(eq(String.format(QUERY_SERVICE_EXPRESSION_TEMPLATE, PUBLICATION_HISTORY_DOCUMENT_TYPE)),
            eq(startTime), any(Content.class));
    verify(reportModel).setReportMap(assertReportMap);
    verify(reportModel).save();
  }

  @Test
  public void getPublicationsInitially() {
    when(reportModel.getLastSaved()).thenReturn(0L);
    Calendar startTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    Date startDate = getDateWithoutTime(new DaysBack(30).getStartDate());
    startTime.setTime(startDate);

    fetchPublicationsHistory.run();

    verify(queryService).poseVersionQuery(eq(String.format(QUERY_SERVICE_EXPRESSION_TEMPLATE, PUBLICATION_HISTORY_DOCUMENT_TYPE)),
            eq(startTime), any(Content.class));

    verify(reportModel).setReportMap(assertReportMap);
    verify(reportModel).setLastSaved(anyLong());
    verify(reportModel).setLastSavedDate(any(Date.class));
    verify(reportModel).save();
  }

  @Test
  public void getPublicationsInitiallyWithDifferentDocumentType() {
    String documentType = "CMArticle";
    when(settingsService.settingWithDefault(eq(PUBLICATION_HISTORY_DOCUMENT_TYPE_KEY), eq(String.class), eq(PUBLICATION_HISTORY_DOCUMENT_TYPE), any(Content.class))).thenReturn(documentType);
    when(reportModel.getLastSaved()).thenReturn(System.currentTimeMillis());
    when(reportModel.getSettings()).thenReturn(ImmutableMap.of(PUBLICATION_HISTORY_DOCUMENT_TYPE_KEY, PUBLICATION_HISTORY_DOCUMENT_TYPE));

    Calendar startTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    Date startDate = getDateWithoutTime(new DaysBack(30).getStartDate());
    startTime.setTime(startDate);

    fetchPublicationsHistory.run();

    verify(queryService).poseVersionQuery(eq(String.format(QUERY_SERVICE_EXPRESSION_TEMPLATE, documentType)),
            eq(startTime), any(Content.class));

    verify(reportModel).setReportMap(assertReportMap);
    verify(reportModel).setLastSaved(anyLong());
    verify(reportModel).setLastSavedDate(any(Date.class));
    verify(reportModel).save();
  }


  @Test
  public void getPublicationsNotNecessary() {
    when(reportModel.getLastSaved()).thenReturn(System.currentTimeMillis());

    fetchPublicationsHistory.run();

    verify(queryService, never()).poseVersionQuery(anyString(), anyString(), any(Calendar.class), any(Content.class));
  }

  @Configuration
  @Import(XmlRepoConfiguration.class)
  public static class LocalConfig {

    private static final String CONTENT_REPO = "classpath:/com/coremedia/testing/contenttest.xml";

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig(CONTENT_REPO);
    }
  }

}
