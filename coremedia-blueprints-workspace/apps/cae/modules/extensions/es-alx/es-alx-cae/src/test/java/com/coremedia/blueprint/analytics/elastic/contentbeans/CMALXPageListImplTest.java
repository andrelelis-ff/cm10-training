package com.coremedia.blueprint.analytics.elastic.contentbeans;


import com.coremedia.blueprint.base.analytics.elastic.ReportModel;
import com.coremedia.blueprint.base.analytics.elastic.TopNReportModelService;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMPicture;
import com.coremedia.cap.content.Content;
import com.coremedia.id.IdProvider;
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
public class CMALXPageListImplTest {

  @InjectMocks
  private CMALXPageListImpl cmalxPageList = new CMALXPageListImpl();

  @Mock
  private TopNReportModelService cmalxBaseListModelServiceFactory;

  @Mock
  private Content content;

  @Mock
  private Content defaultContent;

  @Mock
  private CMLinkable defaultContentLinkable;

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
    when(contentBeanFactory.createBeansFor(Collections.singletonList(defaultContent), CMLinkable.class)).thenReturn(Collections.singletonList(defaultContentLinkable));
    when(content.getString(CMALXBaseList.ANALYTICS_PROVIDER)).thenReturn(service);
    when(content.getLinks(CMALXPageList.DEFAULT_CONTENT)).thenReturn(Collections.singletonList(defaultContent));
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
    List<CMLinkable> objects = cmalxPageList.getItemsUnfiltered();

    assertEquals(1, objects.size());
    assertEquals(cmPicture, objects.get(0));
  }


  @Test
  public void getItemsUnfiltered_defaultContent() {
    when(reportModel.getReportData()).thenReturn(Collections.<String>emptyList());
    List objects = cmalxPageList.getItemsUnfiltered();

    assertEquals(1, objects.size());
    assertEquals(defaultContentLinkable, objects.get(0));
  }
}
