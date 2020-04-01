package com.coremedia.blueprint.analytics.elastic.contentbeans;

import com.coremedia.blueprint.base.analytics.elastic.ReportModel;
import com.coremedia.blueprint.base.analytics.elastic.TopNReportModelService;
import com.coremedia.blueprint.common.contentbeans.CMCollection;
import com.coremedia.blueprint.common.contentbeans.CMPicture;
import com.coremedia.cap.content.Content;
import com.coremedia.id.IdProvider;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanDefinition;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CMALXEventListImplTest {
  @InjectMocks
  private CMALXEventListImpl cmalxEventList = new CMALXEventListImpl();

  @Mock
  private TopNReportModelService cmalxBaseListModelServiceFactory;

  @Mock
  private Content content;

  @Mock
  private Content defaultContent;

  @Mock
  private CMPicture defaultContentPicture;

  @Mock
  private ReportModel reportModel;

  @Mock
  private IdProvider idProvider;

  @Mock
  private CMPicture cmPicture;

  @Mock
  private ContentBeanDefinition contentBeanDefinition;

  @Mock
  private ContentBeanFactory contentBeanFactory;

  @Before
  public void setup() {
    String service = "service";
    when(contentBeanDefinition.getContentBeanFactory()).thenReturn(contentBeanFactory);
    when(contentBeanFactory.createBeansFor(Collections.singletonList(defaultContent), ContentBean.class)).thenReturn(Collections.singletonList(defaultContentPicture));
    when(content.getString(CMALXBaseList.ANALYTICS_PROVIDER)).thenReturn(service);
    when(content.getLinks(CMCollection.ITEMS)).thenReturn(Collections.singletonList(defaultContent));
    when(cmalxBaseListModelServiceFactory.getReportModel(content, service)).thenReturn(reportModel);
  }

  @Test
  public void getItemsUnfiltered() {
    String objectId = "1234";
    String teasableId = "5678";
    List<String> reportData = Arrays.asList(objectId, teasableId);
    when(reportModel.getReportData()).thenReturn(reportData);
    when(idProvider.parseId(objectId)).thenReturn(objectId);
    when(idProvider.parseId(teasableId)).thenReturn(cmPicture);
    List objects = cmalxEventList.getItemsUnfiltered();

    assertEquals(1, objects.size());
    assertEquals(cmPicture, objects.get(0));
  }

  @Test
  public void getItemsUnfiltered_defaultContent() {
    when(reportModel.getReportData()).thenReturn(Collections.<String>emptyList());
    List objects = cmalxEventList.getItemsUnfiltered();

    assertEquals(1, objects.size());
    assertEquals(defaultContentPicture, objects.get(0));
  }
}
