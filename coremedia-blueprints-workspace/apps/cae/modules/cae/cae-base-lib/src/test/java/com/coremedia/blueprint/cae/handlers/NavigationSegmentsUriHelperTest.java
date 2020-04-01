package com.coremedia.blueprint.cae.handlers;

import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.cache.Cache;
import com.coremedia.cap.content.Content;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link NavigationSegmentsUriHelper}
 */
public class NavigationSegmentsUriHelperTest {

  private String rootSegment = "root";
  private String child1Segment = "child1";
  private String child2Segment = "child2";

  private NavigationSegmentsUriHelper testling;
  private CMNavigation child2Navigation;

  @Before
  public void setUp() throws Exception {

    // 1. --- set up testling.
    // Tell Mockito that we want to use real methods and only use mocked methods when using "doReturn().when().method()"
    testling = mock(NavigationSegmentsUriHelper.class, Mockito.CALLS_REAL_METHODS);
    ContentBeanFactory contentBeanFactory = mock(ContentBeanFactory.class);
    Cache cache = mock(Cache.class);

    testling.setCache(cache);
    testling.setContentBeanFactory(contentBeanFactory);

    // 2. --- set up mocked content
    Content rootNavigationContent = mock(Content.class);
    CMNavigation rootNavigation = mock(CMNavigation.class);
    when(rootNavigation.getSegment()).thenReturn(rootSegment);
    when(rootNavigation.getContent()).thenReturn(rootNavigationContent);
    //#lookupRootSegment is the only method that should not be called directly
    doReturn(rootNavigation).when(testling).lookupRootSegment(rootSegment);

    CMNavigation child1Navigation = mock(CMNavigation.class);
    when(child1Navigation.getSegment()).thenReturn(child1Segment);

    Content child2NavigationContent = mock(Content.class);
    child2Navigation = mock(CMNavigation.class);
    when(child2Navigation.getSegment()).thenReturn(child2Segment);
    when(contentBeanFactory.createBeanFor(child2NavigationContent, CMNavigation.class)).thenReturn(child2Navigation);

    // 3. --- mock cascade of child documents linked to each other.
    List<CMNavigation> navigationPathList = new ArrayList<>();
    navigationPathList.add(rootNavigation);
    navigationPathList.add(child1Navigation);
    navigationPathList.add(child2Navigation);
//    //when().thenReturn() can't be used with methods that return type wildcards
    doReturn(navigationPathList).when(child2Navigation).getNavigationPathList();

    List<? extends Linkable> children1 = Arrays.asList(child1Navigation);
    doReturn(children1).when(rootNavigation).getChildren();

    List<? extends Linkable> children2 = Arrays.asList(child2Navigation);
    doReturn(children2).when(child1Navigation).getChildren();
  }

  /**
   * Tests {@link NavigationSegmentsUriHelper#parsePath(java.util.List)}
   */
  @Test
  public void testParsePath() throws Exception {
    Navigation actualPath = testling.parsePath(asList(rootSegment, child1Segment, child2Segment));
    assertEquals("CMNavigation does not match", child2Navigation, actualPath);
  }

  @Test
  public void testParseNavigationPath() throws Exception {
    Navigation actualPath = testling.parsePath(rootSegment + NavigationSegmentsUriHelper.SEGMENT_DELIM + child1Segment + NavigationSegmentsUriHelper.SEGMENT_DELIM + child2Segment);
    assertEquals("CMNavigation does not match", child2Navigation, actualPath);
  }
}
