package com.coremedia.blueprint.caefeeder;

import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NavigationPathPropertyConverterTest {

  @Mock
  private com.coremedia.blueprint.base.caefeeder.TreePathKeyFactory<Content> treePathKeyFactory;

  private NavigationPathPropertyConverter testling = new NavigationPathPropertyConverter();

  @Before
  public void setUp() throws Exception {
    testling.setNavigationPathKeyFactory(treePathKeyFactory);
  }

  @Test
  public void testConvertNull() {
    assertEquals(ImmutableList.of(), testling.convertValue(null));
  }

  @Test
  public void testConvertType() {
    assertEquals(List.class, testling.convertType(Collection.class));
  }

  @Test
  public void testConvert() {
    CMNavigation bean = mock(CMNavigation.class);
    Content content = content(42);
    when(bean.getContent()).thenReturn(content);

    Content root = content(40);
    when(treePathKeyFactory.getPath(content)).thenReturn(ImmutableList.of(root, content));

    assertEquals(ImmutableList.of("/40/42"), testling.convertValue(ImmutableList.of(bean)));
  }

  private static Content content(int id) {
    Content content = mock(Content.class, String.valueOf(id));
    when(content.getId()).thenReturn(IdHelper.formatContentId(id));
    return content;
  }
}