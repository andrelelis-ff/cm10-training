package com.coremedia.blueprint.analytics.elastic.tasks;

import com.coremedia.blueprint.base.analytics.elastic.TopNReportModelService;
import com.coremedia.blueprint.analytics.elastic.retrieval.AnalyticsServiceProvider;
import com.coremedia.blueprint.base.analytics.elastic.validation.ResultItemValidationService;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.elastic.core.api.tenant.TenantService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class FetchReportsTaskTest {

  @Mock
  private CMALXBaseListService baseListInstances;

  @Mock
  private TenantService tenantService;

  @Mock
  private AnalyticsServiceProvider serviceProvider;

  @Mock
  private RootContentProcessingTaskHelper rootContentProcessingTaskHelper;

  @Mock
  private TopNReportModelService modelService;

  @Mock
  private SitesService sitesService;

  /**
   * This is just a dummy test to guide you to the real thing: EsAlxRetrievalApplicationContextTest
   */
  @Test
  public void testConstructor() {
    new FetchReportsTask(baseListInstances, modelService, tenantService, mock(ResultItemValidationService.class),rootContentProcessingTaskHelper, sitesService);
  }
}