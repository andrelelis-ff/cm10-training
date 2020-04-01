package com.coremedia.blueprint.analytics.elastic.rest;

import com.coremedia.blueprint.base.analytics.elastic.PageViewReportModelService;
import com.coremedia.blueprint.base.analytics.elastic.PageViewResult;
import com.coremedia.blueprint.base.analytics.elastic.PublicationReportModelService;
import com.coremedia.blueprint.base.analytics.elastic.ReportModel;
import com.coremedia.blueprint.base.analytics.elastic.util.RetrievalUtil;
import com.coremedia.blueprint.base.navigation.context.ContextStrategy;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.rest.exception.WebApplicationException;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.coremedia.blueprint.base.analytics.elastic.ReportModel.REPORT_DATE_FORMAT;
import static com.coremedia.elastic.core.test.Injection.inject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AlxResourceTest.LocalConfig.class)
public class AlxResourceTest {

  public static final String DEFAULT_SERVICE_PROVIDER = "googleAnalytics";

  @InjectMocks
  private AlxResource alxResource = new AlxResource();

  @Mock
  private PageViewReportModelService pageViewReportModelService;

  @Mock
  private ReportModel pageViewReportModel;

  @Mock
  private ReportModel publicationReportModel;

  @Mock
  private PageViewResult pageViewResult;

  @Mock
  private Map<String, Long> reportMap;

  @Mock
  private Content content;

  @Mock
  private Content navigation;

  @Mock
  private ContextStrategy<Content, Content> contentContextStrategy;

  @Mock
  private SettingsService settingsService;

  @Mock
  private PublicationReportModelService publicationReportModelService;

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private ContentRepository contentRepository;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    inject(alxResource, contentRepository);
    inject(alxResource, publicationReportModelService);
    inject(alxResource, pageViewReportModelService);
    inject(alxResource, settingsService);

    when(settingsService.setting(eq(RetrievalUtil.DOCUMENT_PROPERTY_ANALYTICS_PROVIDER), eq(String.class), anyObject())).thenReturn("");

    when(pageViewReportModelService.getPageViewResult(any(Content.class), eq(DEFAULT_SERVICE_PROVIDER))).thenReturn(pageViewResult);
    when(pageViewResult.getReportModel()).thenReturn(pageViewReportModel);
    when(pageViewResult.getTimeStamp()).thenReturn(new Date());

    when(publicationReportModelService.getReportModel(any(Content.class))).thenReturn(publicationReportModel);
    when(pageViewReportModel.getReportMap()).thenReturn(reportMap);
    when(publicationReportModel.getReportMap()).thenReturn(reportMap);

    List<Content> navigations = new ArrayList<>();
    navigations.add(navigation);
    doReturn(navigations).when(contentContextStrategy).findContextsFor(content);
  }

  @Test
  public void getAlxDataNullValues() {
    when(publicationReportModelService.getReportModel(null, null)).thenReturn(publicationReportModel);
    when(reportMap.get(any(String.class))).thenReturn(0L);

    ReportResult result = alxResource.getAlxData("4", null);

    assertNotNull(result.getData());
    assertEquals(7, result.getData().size());
    assertNotNull(result.getTimeStamp());
  }

  @Test
  public void getAlxDataNoTimeStamp() {
    when(reportMap.get(any(String.class))).thenReturn(0L);
    when(pageViewResult.getTimeStamp()).thenReturn(null);
    ReportResult result = alxResource.getAlxData("4", null);
    assertNotNull(result.getData());
    assertEquals(0, result.getData().size());
    assertNull(result.getTimeStamp());
  }

  @Test
  public void getAlxData() {
    when(reportMap.get(any(String.class))).thenReturn(3L);
    ReportResult result = alxResource.getAlxData("4", null);
    List<AlxData> alxData = result.getData();
    assertNotNull(result.getData());
    assertEquals(7, result.getData().size());
    Date today = new Date();
    for (int i = 0; i < 7; i++) {
      Date date = DateUtils.addDays(today, -(6 - i));
      DateFormat dateFormat = new SimpleDateFormat(REPORT_DATE_FORMAT, Locale.getDefault());
      assertEquals(dateFormat.format(date), alxData.get(i).getKey());
      assertEquals(3, alxData.get(i).getValue());
    }
  }

  @Test
  public void getAlxDataWithInvalidRange() {
    when(reportMap.get(any(String.class))).thenReturn(3L);
    ReportResult result = alxResource.getAlxData("4", 0);
    List<AlxData> alxData = result.getData();
    assertNotNull(alxData);
    assertEquals(7, alxData.size());
    Date today = new Date();
    for (int i = 0; i < 7; i++) {
      Date date = DateUtils.addDays(today, -(6 - i));
      DateFormat dateFormat = new SimpleDateFormat(REPORT_DATE_FORMAT, Locale.getDefault());
      assertEquals(dateFormat.format(date), alxData.get(i).getKey());
      assertEquals(3, alxData.get(i).getValue());
    }
  }

  @Test
  public void getAlxDataForTimeRange() {
    when(reportMap.get(any(String.class))).thenReturn(3L);
    ReportResult result = alxResource.getAlxData("4", 3);
    List<AlxData> alxData = result.getData();
    assertNotNull(alxData);
    assertEquals(3, alxData.size());
    Date today = new Date();
    for (int i = 0; i < 3; i++) {
      Date date = DateUtils.addDays(today, -(2 - i));
      DateFormat dateFormat = new SimpleDateFormat(REPORT_DATE_FORMAT, Locale.getDefault());
      assertEquals(dateFormat.format(date), alxData.get(i).getKey());
      assertEquals(3, alxData.get(i).getValue());
    }
  }

  @Test(expected = WebApplicationException.class)
  public void getAlxDataNotCMLinkable() {
    alxResource.getAlxData("123456", null);
  }

  @Test
  public void getPublicationData() {
    when(reportMap.get(any(String.class))).thenReturn(3L);
    when(publicationReportModel.getReportMap()).thenReturn(reportMap);
    when(publicationReportModel.getLastSaved()).thenReturn(System.currentTimeMillis());
    ReportResult result = alxResource.getPublicationData("4", 7);

    List<AlxData> alxData = result.getData();
    assertNotNull(result.getData());
    assertEquals(7, result.getData().size());
    Date today = new Date();
    for (int i = 0; i < 7; i++) {
      Date date = DateUtils.addDays(today, -(6 - i));
      DateFormat dateFormat = new SimpleDateFormat(REPORT_DATE_FORMAT, Locale.getDefault());
      assertEquals(dateFormat.format(date), alxData.get(i).getKey());
      assertEquals(3, alxData.get(i).getValue());
    }
  }

  @Test
  public void getNoPublicationData() {
    when(reportMap.get(any(String.class))).thenReturn(0L);
    when(publicationReportModel.getReportMap()).thenReturn(reportMap);
    when(publicationReportModel.getLastSaved()).thenReturn(0L);
    ReportResult result = alxResource.getPublicationData("4", 7);

    assertNotNull(result.getData());
    assertEquals(0, result.getData().size());
  }

  @Test
  public void getEmptyPublicationData() {
    when(reportMap.get(any(String.class))).thenReturn(0L);
    when(publicationReportModel.getReportMap()).thenReturn(reportMap);
    when(publicationReportModel.getLastSaved()).thenReturn(System.currentTimeMillis());
    ReportResult result = alxResource.getPublicationData("4", 7);

    List<AlxData> alxData = result.getData();
    assertNotNull(result.getData());
    assertEquals(7, result.getData().size());
    Date today = new Date();
    for (int i = 0; i < 7; i++) {
      Date date = DateUtils.addDays(today, -(6 - i));
      DateFormat dateFormat = new SimpleDateFormat(REPORT_DATE_FORMAT, Locale.getDefault());
      assertEquals(dateFormat.format(date), alxData.get(i).getKey());
      assertEquals(0, alxData.get(i).getValue());
    }
  }

  @Test(expected = WebApplicationException.class)
  public void getPublicationDataNotCMLinkable() {
    alxResource.getPublicationData("123456", null);
  }

  @Configuration
  @ImportResource(
          value = "classpath:/com/coremedia/blueprint/base/multisite/bpbase-multisite-services.xml",
          reader = ResourceAwareXmlBeanDefinitionReader.class
  )
  @Import(XmlRepoConfiguration.class)
  public static class LocalConfig {
    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public XmlUapiConfig xmlUapiConfig() {
      return new XmlUapiConfig("classpath:/com/coremedia/testing/contenttest.xml");
    }
  }
}
