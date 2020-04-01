package com.coremedia.blueprint.coderesources;

import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ThemeServiceTest {

  @Mock
  private Content nav1, noNav, nav2, nav3;

  @Mock
  private Content theme1, theme3;

  @Mock
  private ContentType cmNavigation, noNavigation;

  @Mock
  private TreeRelation<Content> treeRelation;

  private ThemeService testling;

  @Before
  public void setup() {
    when(treeRelation.pathToRoot(nav3)).thenReturn(Arrays.asList(nav1, noNav, nav2, nav3));
    when(nav3.getType()).thenReturn(cmNavigation);
    when(noNav.getType()).thenReturn(noNavigation);
    when(nav2.getType()).thenReturn(cmNavigation);
    when(nav1.getType()).thenReturn(cmNavigation);
    when(cmNavigation.isSubtypeOf("CMNavigation")).thenReturn(true);
    when(noNavigation.isSubtypeOf("CMNavigation")).thenReturn(false);
    testling = new ThemeService(treeRelation);
  }

  @Test
  public void testDirectTheme() {
    when(nav3.getLink("theme")).thenReturn(theme3);
    Content actual = testling.theme(nav3, null);
    assertTrue(theme3 == actual);
  }

  @Test
  public void testInheritedTheme() {
    when(nav1.getLink("theme")).thenReturn(theme1);
    Content actual = testling.theme(nav3, null);
    assertTrue(theme1 == actual);
  }

  @Test
  public void testNoTheme() {
    Content actual = testling.theme(nav3, null);
    assertNull(actual);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoNavigation() {
    testling.theme(noNav, null);
  }
}
