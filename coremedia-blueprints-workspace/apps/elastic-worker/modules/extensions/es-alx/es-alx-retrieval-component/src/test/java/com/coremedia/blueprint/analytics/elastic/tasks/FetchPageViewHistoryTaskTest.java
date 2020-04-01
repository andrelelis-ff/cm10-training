package com.coremedia.blueprint.analytics.elastic.tasks;

import com.coremedia.blueprint.analytics.elastic.retrieval.AnalyticsServiceProvider;
import com.coremedia.blueprint.base.analytics.elastic.PageViewReportModelService;
import com.coremedia.blueprint.base.analytics.elastic.PageViewTaskReportModelService;
import com.coremedia.blueprint.base.analytics.elastic.ReportModel;
import com.coremedia.blueprint.base.analytics.elastic.validation.ResultItemValidationService;
import com.coremedia.blueprint.base.elastic.tenant.TenantSiteMapping;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.elastic.core.api.tenant.TenantService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.coremedia.blueprint.base.analytics.elastic.ReportModel.REPORT_DATE_FORMAT;
import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {FetchPageViewHistoryTaskTest.LocalConfig.class, XmlRepoConfiguration.class})
public class FetchPageViewHistoryTaskTest {

  static final String UNKNOWN_SERVICE = "unknown";
  static final String NON_LINKABLE_CONTENT_ID = "404";
  static final String TENANT = "tenant";
  static final String SERVICE_KEY = "service";
  static final String ARTICLE_CONTENT_ID = "1234";
  static final String NO_CONTENT_ID = "1230";
  static final String CHANNEL_CONTENT_ID = "12346";

  @Inject
  private ContentRepository contentRepository;
  @Inject
  private SettingsService settingsService;

  private TenantService tenantService;
  private PageViewReportModelService modelService;
  private PageViewTaskReportModelService taskReportModelService;
  private AnalyticsServiceProvider analyticsServiceProvider;
  private FetchPageViewHistoryTask task;
  private ReportModel taskModelForRoot;
  private ReportModel pageViewReportModel;

  private final DateFormat dateFormat = new SimpleDateFormat(REPORT_DATE_FORMAT, Locale.getDefault());

  private Content channelContent;
  private Content articleContent;
  private Map<String, Object> analyticsSettings;

  @Before
  public void setup() {
    taskModelForRoot = mock(ReportModel.class);
    pageViewReportModel = mock(ReportModel.class);
    tenantService = mock(TenantService.class);
    modelService = mock(PageViewReportModelService.class);
    taskReportModelService= mock(PageViewTaskReportModelService.class);
    analyticsServiceProvider = mock(AnalyticsServiceProvider.class);
    AnalyticsServiceProvider unconfigureAnalyticsServiceProvider = mock(AnalyticsServiceProvider.class);
    ResultItemValidationService resultItemValidationService = mock(ResultItemValidationService.class);

    final TenantSiteMapping tenantSiteMappingHelper = mock(TenantSiteMapping.class);

    final FetchPageViewHistoryRootContentProcessor fetchPageViewHistoryRootContentProcessor = new FetchPageViewHistoryRootContentProcessor(modelService, taskReportModelService, contentRepository, resultItemValidationService);
    final RootContentProcessingTaskHelper helper = new RootContentProcessingTaskHelper(tenantSiteMappingHelper, tenantService, settingsService, asList(analyticsServiceProvider, unconfigureAnalyticsServiceProvider));
    task = new FetchPageViewHistoryTask(fetchPageViewHistoryRootContentProcessor, helper);

    channelContent = getContent(CHANNEL_CONTENT_ID);
    articleContent = getContent(ARTICLE_CONTENT_ID);

    when(tenantSiteMappingHelper.getTenantSiteMap()).thenReturn(Collections.singletonMap(TENANT, Collections.singleton(channelContent)));

    // ALX settings for saved task model
    analyticsSettings = new HashMap<>();
    analyticsSettings.put("applicationName", "CoreMedia");

    when(taskModelForRoot.getSettings()).thenReturn(analyticsSettings);
    when(modelService.getReportModel(articleContent, SERVICE_KEY)).thenReturn(pageViewReportModel);
    when(tenantService.getCurrent()).thenReturn(TENANT);

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) {
        //noinspection unchecked
        Collection<ReportModel> models = (Collection<ReportModel>) invocation.getArguments()[0];
        for(ReportModel model : models) {
          model.save();
        }
        return null;
      }
    }).when(modelService).saveAll(anyCollection());

    when(analyticsServiceProvider.getServiceKey()).thenReturn(SERVICE_KEY);
    when(unconfigureAnalyticsServiceProvider.getServiceKey()).thenReturn(UNKNOWN_SERVICE);
    when(resultItemValidationService.filterValidResultItems(anyCollection(), anyString())).thenReturn(asList(ARTICLE_CONTENT_ID));
  }

  public Content getContent(String contentId) {
    return contentRepository.getContent(contentId);
  }

  @Test
  public void run() throws Exception {
    Map<String, Map<String, Long>> data = new HashMap<>();
    Map<String, Long> pageViews = new HashMap<>();
    String today = dateFormat.format(new Date());
    pageViews.put(today, 13L);
    data.put(ARTICLE_CONTENT_ID, pageViews);
    when(analyticsServiceProvider.fetchPageViews(any(Content.class), anyMap())).thenReturn(data);
    when(taskReportModelService.getReportModel(channelContent, SERVICE_KEY)).thenReturn(taskModelForRoot);
    when(modelService.getReportModel(any(Content.class), eq(SERVICE_KEY))).thenReturn(pageViewReportModel);

    task.run();

    verify(pageViewReportModel).setReportMap(pageViews);
    verify(pageViewReportModel).setLastSaved(anyLong());
    verify(pageViewReportModel).setLastSavedDate(any(Date.class));
    verify(pageViewReportModel).save();
    verify(taskModelForRoot).save();
    verify(taskModelForRoot).setLastSaved(anyLong());
    verify(taskModelForRoot).setLastSavedDate(any(Date.class));
  }

  @Test
  public void runButNoData() throws Exception {

    when(taskReportModelService.getReportModel(channelContent, SERVICE_KEY)).thenReturn(taskModelForRoot);
    when(analyticsServiceProvider.fetchPageViews(any(Content.class), anyMap())).thenReturn(null);

    task.run();

    verify(taskModelForRoot).save();
    verify(taskModelForRoot).setLastSaved(anyLong());
    verify(taskModelForRoot).setLastSavedDate(any(Date.class));
  }

  @Test
  public void runWithContentException() throws Exception {
    Map<String, Map<String, Long>> data = new HashMap<>();
    Map<String, Long> pageViews = new HashMap<>();
    String today = dateFormat.format(new Date());
    pageViews.put(today, 13L);
    data.put(NO_CONTENT_ID, pageViews);

    when(analyticsServiceProvider.fetchPageViews(any(Content.class), anyMap())).thenReturn(data);
    when(taskReportModelService.getReportModel(channelContent, SERVICE_KEY)).thenReturn(taskModelForRoot);

    task.run();

    verify(taskModelForRoot).save();
  }

  @Test
  public void runWithIdException() throws Exception {
    Map<String, Map<String, Long>> data = new HashMap<>();
    Map<String, Long> pageViews = new HashMap<>();
    String today = dateFormat.format(new Date());
    pageViews.put(today, 13L);
    data.put("invalid", pageViews);

    when(analyticsServiceProvider.fetchPageViews(any(Content.class), anyMap())).thenReturn(data);
    when(taskReportModelService.getReportModel(channelContent, SERVICE_KEY)).thenReturn(taskModelForRoot);

    task.run();

    verify(taskModelForRoot).save();
  }

  @Test
  public void runWithNonLinkable() throws Exception {
    Map<String, Map<String, Long>> data = new HashMap<>();
    Map<String, Long> pageViews = new HashMap<>();
    String today = dateFormat.format(new Date());
    pageViews.put(today, 13L);
    data.put(NON_LINKABLE_CONTENT_ID, pageViews);
    when(analyticsServiceProvider.fetchPageViews(any(Content.class), anyMap())).thenReturn(data);
    when(taskReportModelService.getReportModel(channelContent, SERVICE_KEY)).thenReturn(taskModelForRoot);

    task.run();

    verify(taskModelForRoot).save();
    verify(taskModelForRoot).setLastSaved(anyLong());
    verify(taskModelForRoot).setLastSavedDate(any(Date.class));
    verify(modelService, never()).getReportModel(any(Content.class), anyString());
  }

  @Test
  public void runWithNoRootForTenant() throws Exception {
    when(tenantService.getCurrent()).thenReturn("unknownTenant");

    task.run();

    verify(analyticsServiceProvider, never()).fetchPageViews(any(Content.class), anyMap());
    verify(modelService, never()).getReportModel(articleContent, SERVICE_KEY);
  }

  @Test
  public void runWithConfigChanges() throws Exception {
    long start = System.currentTimeMillis();
    when(taskModelForRoot.getLastSaved()).thenReturn(start - 1);
    when(taskReportModelService.getReportModel(channelContent, SERVICE_KEY)).thenReturn(taskModelForRoot);
    analyticsSettings.put("password", "old");

    Map<String, Map<String, Long>> data = new HashMap<>();
    Map<String, Long> pageViews = new HashMap<>();
    String today = dateFormat.format(new Date());
    pageViews.put(today, 13L);
    data.put(ARTICLE_CONTENT_ID, pageViews);
    when(analyticsServiceProvider.fetchPageViews(any(Content.class), anyMap())).thenReturn(data);

    task.run();

    verify(pageViewReportModel).setReportMap(pageViews);
    verify(pageViewReportModel).setLastSaved(anyLong());
    verify(pageViewReportModel).setLastSavedDate(any(Date.class));
    verify(pageViewReportModel).save();
    verify(taskModelForRoot).save();
    verify(taskModelForRoot).setLastSaved(anyLong());
    verify(taskModelForRoot).setLastSavedDate(any(Date.class));
  }

  @Test
  public void runWithAdditionalConfig() throws Exception {
    long start = System.currentTimeMillis();
    when(taskModelForRoot.getLastSaved()).thenReturn(start - 1);

    when(taskReportModelService.getReportModel(channelContent, SERVICE_KEY)).thenReturn(taskModelForRoot);
    analyticsSettings.remove("password");

    Map<String, Map<String, Long>> data = new HashMap<>();
    Map<String, Long> pageViews = new HashMap<>();
    String today = dateFormat.format(new Date());
    pageViews.put(today, 13L);
    data.put(ARTICLE_CONTENT_ID, pageViews);
    when(analyticsServiceProvider.fetchPageViews(any(Content.class), anyMap()))
            .thenReturn(data);

    task.run();

    verify(pageViewReportModel).setReportMap(pageViews);
    verify(pageViewReportModel).setLastSaved(anyLong());
    verify(pageViewReportModel).setLastSavedDate(any(Date.class));
    verify(pageViewReportModel).save();
    verify(taskModelForRoot).save();
    verify(taskModelForRoot).setLastSaved(anyLong());
    verify(taskModelForRoot).setLastSavedDate(any(Date.class));
  }

  @Test
  public void runWithNoUpdate() throws Exception {
    // make sure that the model's effective settings are equal to the channels effective service settings
    analyticsSettings.put("password", "password");

    long start = System.currentTimeMillis();
    when(taskModelForRoot.getLastSaved()).thenReturn(start - 1);
    when(taskReportModelService.getReportModel(channelContent, SERVICE_KEY)).thenReturn(taskModelForRoot);

    task.run();

    verify(analyticsServiceProvider, never()).fetchPageViews(any(Content.class), anyMap());
  }

  @Test
  public void runWithoutSettings() throws Exception {
    Map<String, Map<String, Long>> data = new HashMap<>();
    Map<String, Long> pageViews = new HashMap<>();
    String today = dateFormat.format(new Date());
    pageViews.put(today, 13L);
    data.put(ARTICLE_CONTENT_ID, pageViews);

    String keyOfUnconfiguredService = SERVICE_KEY + getClass().getName();
    when(analyticsServiceProvider.getServiceKey()).thenReturn(keyOfUnconfiguredService);
    when(analyticsServiceProvider.fetchPageViews(any(Content.class), anyMap())).thenReturn(data);
    when(taskReportModelService.getReportModel(channelContent, keyOfUnconfiguredService)).thenReturn(taskModelForRoot);

    task.run();

    verify(pageViewReportModel, never()).setReportMap(pageViews);
    verify(pageViewReportModel, never()).setLastSaved(anyLong());
    verify(pageViewReportModel, never()).setLastSavedDate(any(Date.class));
    verify(pageViewReportModel, never()).save();
    verify(taskModelForRoot, never()).save();
    verify(taskModelForRoot, never()).setLastSaved(anyLong());
    verify(taskModelForRoot, never()).setLastSavedDate(any(Date.class));
  }

  @Configuration
  @ImportResource(value = {
          "classpath:/framework/spring/blueprint-contentbeans.xml"
  },
          reader = com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader.class)
  public static class LocalConfig {
    @Bean
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig("classpath:/com/coremedia/testing/contenttest.xml");
    }

  }
}
