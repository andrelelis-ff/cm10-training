package com.coremedia.blueprint.analytics.elastic.retrieval;

import com.coremedia.blueprint.analytics.elastic.tasks.FetchPageViewHistoryTask;
import com.coremedia.blueprint.analytics.elastic.tasks.FetchReportsTask;
import com.coremedia.blueprint.base.analytics.elastic.PageViewReportModelService;
import com.coremedia.blueprint.base.analytics.elastic.PageViewTaskReportModelService;
import com.coremedia.blueprint.base.analytics.elastic.ReportModel;
import com.coremedia.blueprint.base.analytics.elastic.ReportModelService;
import com.coremedia.blueprint.base.analytics.elastic.TopNReportModelService;
import com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.elastic.core.api.models.Model;
import com.coremedia.elastic.core.api.models.Query;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.coremedia.blueprint.base.analytics.elastic.ReportModel.REPORT_DATE_FORMAT;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = {
        "classpath:/com/coremedia/blueprint/analytics/elastic/retrieval/EsAlxRetrievalApplicationContextTest-context.xml",
        "classpath:/com/coremedia/blueprint/base/navigation/context/bpbase-default-contextstrategy.xml",
        "classpath:/com/coremedia/blueprint/base/multisite/bpbase-multisite-services.xml"
})
@Configuration
@ComponentScan("com.coremedia.cap.common.xml")
@TestPropertySource(properties = {
        "elastic.core.persistence=memory",
        "repository.params.contentxml=classpath:/com/coremedia/testing/contenttest.xml",
        "repository.params.userxml=classpath:/com/coremedia/testing/usertest.xml",
        "tenant.default=tenant",
        "models.createIndexes=false",
        "elastic.solr.lazyIndexCreation=true"
})
public class EsAlxRetrievalApplicationContextTest {

  private static final String SERVICE = "service"; // compare with 12345settings.xml
  static final Object APPLICATION_NAME = "CoreMedia";
  static final ImmutableMap<String, Object> EFFECTIVE_SETTINGS = ImmutableMap.of("applicationName", APPLICATION_NAME, "password", "password", "timeRange", 14, "maxLength", 4, "interval", 50000);
  static final List<String> ARTICLES = asList("113110", "1131110", "113152", "206", "886", "1234");
  static final List<String> TOP_4_ARTICLES = asList("contentbean:113110", "contentbean:1131110", "contentbean:113152", "contentbean:206");

  @Inject
  private AnalyticsServiceProvider analyticsServiceProvider;

  @Inject
  private FetchReportsTask fetchReportsTask;

  @Inject
  private TopNReportModelService topNReportModelService;

  @Inject
  private ContentRepository contentRepository;

  @Inject
  private FetchPageViewHistoryTask fetchPageViewHistoryTask;

  @Inject
  private PageViewTaskReportModelService pageViewTaskReportModelService;

  @Inject
  private PageViewReportModelService pageViewReportModelService;

  @Inject
  private SettingsService settingsService;

  private Content pageList;
  private long start;

  @Before
  public void setup() {
    pageList = contentRepository.getContent("12348"); // compare with contenttest.xml

    when(analyticsServiceProvider.getServiceKey()).thenReturn(SERVICE);
    when(analyticsServiceProvider.computeEffectiveRetrievalSettings(eq(pageList), any(Content.class)))
    .then(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        final Object[] args = invocation.getArguments();
        return RetrievalUtil.computeEffectiveRetrievalSettings(SERVICE, EFFECTIVE_SETTINGS, (Content)args[0], (Content) args[1], settingsService);
      }
    });

    assertEquals(contentRepository.getContentType("CMALXPageList"), pageList.getType());
    start = System.currentTimeMillis();
  }

  @After
  public void teardown() {
    reset(analyticsServiceProvider);

    removeModels(topNReportModelService);
    removeModels(pageViewReportModelService);
    removeModels(pageViewTaskReportModelService);
  }

  private void removeModels(ReportModelService service) {
    for(Model model : service.query().fetch()){
      try {
        model.remove();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Test
  public void runFetchReportsTaskWithConfiguration() throws Exception {
    when(analyticsServiceProvider.fetchDataFor(eq(pageList), eq(EFFECTIVE_SETTINGS))).thenReturn(ARTICLES);

    fetchReportsTask.run();
    verify(analyticsServiceProvider).computeEffectiveRetrievalSettings(eq(pageList), any(Content.class));
    verify(analyticsServiceProvider).fetchDataFor(eq(pageList), any(Map.class));

    final ReportModel reportModel = topNReportModelService.getReportModel(pageList, SERVICE);
    assertEquals(TOP_4_ARTICLES, reportModel.getReportData());
    recentlySaved(reportModel);
  }

  private void recentlySaved(ReportModel reportModel) {
    final long lastSaved = reportModel.getLastSaved();
    assertTrue(lastSaved + " should be greater equals than " + start, lastSaved >= start);
  }

  @Test
  public void runFetchReportsTaskWithException() throws Exception {
    when(analyticsServiceProvider.fetchDataFor(eq(pageList), eq(EFFECTIVE_SETTINGS))).thenThrow(RuntimeException.class);

    fetchReportsTask.run();
    verify(analyticsServiceProvider).computeEffectiveRetrievalSettings(eq(pageList), any(Content.class));
    verify(analyticsServiceProvider).fetchDataFor(eq(pageList), any(Map.class));

    final ReportModel reportModel = topNReportModelService.getReportModel(pageList, SERVICE);
    assertEquals(emptyList(), reportModel.getReportData());
    recentlySaved(reportModel);
  }

  @Test
  public void runFetchReportsTaskForNotConfiguredService() throws Exception {
    final Content pageList = this.pageList.copyTo(this.pageList.getParent(), this.toString());
    pageList.checkOut();
    pageList.set("analyticsProvider", "ignoredService");

    when(analyticsServiceProvider.fetchDataFor(eq(pageList), eq(EFFECTIVE_SETTINGS))).thenReturn(ARTICLES);

    fetchReportsTask.run();
    verify(analyticsServiceProvider, never()).computeEffectiveRetrievalSettings(any(Content.class), same(pageList));
    verify(analyticsServiceProvider, never()).fetchDataFor(same(pageList), anyMap());

    final ReportModel reportModel = topNReportModelService.getReportModel(pageList, SERVICE);
    assertEquals(emptyList(), reportModel.getReportData());
    assertEquals(0, reportModel.getLastSaved());
  }

  @Test
  public void runPageViewHistoryTaskWithoutResult() throws Exception {
    fetchPageViewHistoryTask.run();

    verify(analyticsServiceProvider, atLeast(1)).getServiceKey();
    verify(analyticsServiceProvider).fetchPageViews(any(Content.class), anyMap());

    // root model is saved
    final Query<ReportModel> query = pageViewTaskReportModelService.query();
    assertEquals(1, query.count());
    recentlySaved(query.get());

    // but we have no page views at all
    assertEquals(0, pageViewReportModelService.query().count());
  }

  @Test
  public void runPageViewHistoryTaskWithResult() throws Exception {
    Map<String, Map<String, Long>> data = new HashMap<>();
    String today = new SimpleDateFormat(REPORT_DATE_FORMAT, Locale.getDefault()).format(new Date());
    data.put("not_a_content_id", ImmutableMap.of(today, 13L));
    final String articleId = ARTICLES.get(0);
    data.put(articleId, ImmutableMap.of(today, 5L));
    when(analyticsServiceProvider.fetchPageViews(any(Content.class), anyMap())).thenReturn(data);

    fetchPageViewHistoryTask.run();

    verify(analyticsServiceProvider, atLeast(1)).getServiceKey();
    verify(analyticsServiceProvider).fetchPageViews(any(Content.class), anyMap());

    // root model is saved
    final Query<ReportModel> query = pageViewTaskReportModelService.query();
    assertEquals(1, query.count());
    recentlySaved(query.get());

    // article model is saved
    recentlySaved(pageViewReportModelService.getReportModel(contentRepository.getContent(articleId), SERVICE));
  }

  // =========================================================
  //                      spring config
  // =========================================================

  @Bean
  @Scope("singleton")
  public AnalyticsServiceProvider analyticsServiceProvider(){
    return mock(AnalyticsServiceProvider.class);
  }

}
