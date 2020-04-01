package com.coremedia.blueprint.common.util;

import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMObject;
import com.coremedia.cap.content.Content;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

public class ContentBeanSolrSearchFormatHelperTest {

  public static final int CONTENT_ID_1 = 4711;
  public static final int CONTENT_ID_2 = 4712;

  @Test
  public void testCmObjectToId() throws Exception {
    CMObject cmObject = Mockito.mock(CMObject.class);
    Mockito.when(cmObject.getContentId()).thenReturn(CONTENT_ID_1);
    Assert.assertEquals(CONTENT_ID_1 + "", ContentBeanSolrSearchFormatHelper.cmObjectToId(cmObject));
  }

  @Test
  public void testGetContentBeanId() throws Exception {
    Content content = Mockito.mock(Content.class);
    Mockito.when(content.getId()).thenReturn("coremedia:///cap/content/" + CONTENT_ID_1);
    CMObject cmObject = Mockito.mock(CMObject.class);
    Mockito.when(cmObject.getContent()).thenReturn(content);
    Assert.assertEquals("\"contentbean:" + CONTENT_ID_1 + "\"", ContentBeanSolrSearchFormatHelper.getContentBeanId(cmObject));
  }

  @Test
  public void testCmObjectsToIds() throws Exception {
    CMObject cmObject1 = Mockito.mock(CMObject.class);
    CMObject cmObject2 = Mockito.mock(CMObject.class);
    Mockito.when(cmObject1.getContentId()).thenReturn(CONTENT_ID_1);
    Mockito.when(cmObject2.getContentId()).thenReturn(CONTENT_ID_2);
    List<CMObject> list = new ArrayList<>();
    list.add(cmObject1);
    list.add(cmObject2);
    Assert.assertEquals(2, ContentBeanSolrSearchFormatHelper.cmObjectsToIds(list).size());
    Assert.assertTrue(ContentBeanSolrSearchFormatHelper.cmObjectsToIds(list).contains(CONTENT_ID_1 + ""));
    Assert.assertTrue(ContentBeanSolrSearchFormatHelper.cmObjectsToIds(list).contains(CONTENT_ID_2 + ""));
  }

  @Test
  public void testCmNavigationToId() throws Exception {
    CMNavigation parent = Mockito.mock(CMNavigation.class);
    Mockito.when(parent.getContentId()).thenReturn(CONTENT_ID_2);
    CMNavigation child = Mockito.mock(CMNavigation.class);
    Mockito.when(child.getContentId()).thenReturn(CONTENT_ID_1);
    Mockito.when(child.getParentNavigation()).thenReturn(parent);
    Assert.assertEquals("\\/" + CONTENT_ID_2 + "\\/" + CONTENT_ID_1, ContentBeanSolrSearchFormatHelper.cmNavigationToId(child));
  }

  @Test
  public void testCmNavigationsToId() throws Exception {
    CMNavigation parent = Mockito.mock(CMNavigation.class);
    Mockito.when(parent.getContentId()).thenReturn(CONTENT_ID_2);
    CMNavigation child = Mockito.mock(CMNavigation.class);
    Mockito.when(child.getContentId()).thenReturn(CONTENT_ID_1);
    Mockito.when(child.getParentNavigation()).thenReturn(parent);

    List<CMNavigation> list = new ArrayList<>();
    list.add(parent);
    list.add(child);

    List<String> expected = new ArrayList<>();
    expected.add("\\/" + CONTENT_ID_2);
    expected.add("\\/" + CONTENT_ID_2 + "\\/" + CONTENT_ID_1);

    Assert.assertEquals(expected, ContentBeanSolrSearchFormatHelper.cmNavigationsToId(list));
  }
}
