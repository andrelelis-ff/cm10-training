package com.coremedia.livecontext.navigation;

import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.contentbeans.CMExternalChannel;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Locale;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class LiveContextCategoryNavigationTest {

  private static final String SITE_ID = "aSiteId";

  @Mock
  private Category category;

  @Mock
  private LiveContextNavigationTreeRelation treeRelation;

  @Mock
  private CMNavigation rootNavigation;

  @Mock
  private Site site;

  private LiveContextCategoryNavigation testling;

  @Before
  public void setUp() throws Exception {
    initMocks(this);
    when(site.getId()).thenReturn(SITE_ID);
    testling = new LiveContextCategoryNavigation(category, site, treeRelation);
    //Category must have an ID because it is used in equals
    when(category.getId()).thenReturn(CommerceIdParserHelper.parseCommerceIdOrThrow("test:///x/category/anyID"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorParametersCategoryNull() {
    testling = new LiveContextCategoryNavigation(null, site, treeRelation);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorParametersTreeRelationNull() {
    testling = new LiveContextCategoryNavigation(category, site, null);
  }

  @Test
  public void testGetCategory() throws Exception {
    Category actual = testling.getCategory();
    assertThat(actual).as("Category must be the one which is set by constructor").isSameAs(category);
  }

  @Test
  public void testGetChildren() throws Exception {
    Navigation navigation1 = mock(Navigation.class);
    Navigation navigation2 = mock(Navigation.class);

    List<Linkable> children = newArrayList();
    List<Linkable> otherChildren = newArrayList(navigation1, navigation2);

    when(treeRelation.getChildrenOf(testling)).thenReturn(children, otherChildren);
    List<? extends Linkable> actualChildren = testling.getChildren();
    assertThat(actualChildren).as("No children returned from mock, so list is expected to be empty.").isEmpty();

    actualChildren = testling.getChildren();
    assertThat(actualChildren).as("Two children were added before, so two children must exist.").hasSize(2);
  }

  @Test
  public void testGetParentNavigation2() throws Exception {
    Navigation parentCalculatedByTreeRelation = mock(Navigation.class);
    when(treeRelation.getParentOf(testling)).thenReturn(parentCalculatedByTreeRelation);
    Navigation parentNavigation = testling.getParentNavigation();
    assertThat(parentNavigation).isSameAs(parentCalculatedByTreeRelation);
  }

  @Test
  public void testGetContext() throws Exception {
    CMExternalChannel contextCalculatedByTreeRelation = mock(CMExternalChannel.class);
    when(treeRelation.getNearestExternalChannelForCategory(category, site)).thenReturn(contextCalculatedByTreeRelation);
    CMContext context = testling.getContext();
    assertThat(context).isSameAs(contextCalculatedByTreeRelation);
  }

  @Test
  public void testGetParentNavigation2IsNull() throws Exception {
    when(treeRelation.getParentOf(testling)).thenReturn(null);
    Navigation parentNavigation = testling.getParentNavigation();
    assertThat(parentNavigation).isNull();
  }

  @Test
  public void testGetRootNavigation() throws Exception {
    List<Linkable> navigations = newArrayList(rootNavigation, testling);
    when(treeRelation.pathToRoot(testling)).thenReturn(navigations);
    CMNavigation result = testling.getRootNavigation();
    assertThat(result).as("Wrong root navigation").isSameAs(rootNavigation);
  }

  @Test
  public void testGetRootNavigationNoRootFound() throws Exception {
    List<Linkable> navigations = newArrayList();
    when(treeRelation.pathToRoot(testling)).thenReturn(navigations);
    assertThat(testling.getRootNavigation()).isNull();
  }

  @Test
  public void testGetNavigationPathList() throws Exception {
    Navigation navigation1 = new LiveContextCategoryNavigation(category, site, treeRelation);
    List<Linkable> pathToRoot = newArrayList(testling, navigation1);

    when(treeRelation.pathToRoot(testling)).thenReturn(pathToRoot);

    List<? extends Linkable> navigationPathList = testling.getNavigationPathList();
    assertThat(navigationPathList).hasSize(2);
    assertThat(navigationPathList.get(0)).isSameAs(testling);
    assertThat(navigationPathList.get(1)).isSameAs(navigation1);
  }

  @Test
  public void testIsHidden() throws Exception {
    assertThat(testling.isHidden()).isFalse();
  }

  @Test
  public void testGetVisibleChildren() throws Exception {
    LiveContextCategoryNavigation testlingSpy = spy(testling);
    doReturn(newArrayList()).when(testlingSpy).getChildren();
    testlingSpy.getVisibleChildren();
    verify(testlingSpy, times(1)).getChildren();
  }

  @Test
  public void testIsHiddenInSitemap() throws Exception {
    assertThat(testling.isHiddenInSitemap()).isFalse();
  }

  @Test
  public void testGetSitemapChildren() throws Exception {
    LiveContextCategoryNavigation testlingSpy = spy(testling);
    doReturn(newArrayList()).when(testlingSpy).getChildren();
    testlingSpy.getSitemapChildren();
    verify(testlingSpy, times(1)).getChildren();
  }

  @Test
  public void testGetTitle() throws Exception {
    String categoryName = "name";
    when(category.getName()).thenReturn(categoryName);
    String actual = testling.getTitle();
    assertThat(actual).isEqualTo(categoryName);
  }

  @Test
  public void testGetSegment() throws Exception {
    String anySeoSegment = "anySeoSegment";
    when(category.getSeoSegment()).thenReturn(anySeoSegment);
    String actual = testling.getSegment();
    assertThat(actual).isEqualTo(anySeoSegment);
  }

  @Test
  public void testIsRoot() throws Exception {
    testling.isRoot();
  }

  @Test
  public void testGetLocale() throws Exception {
    Locale expected = Locale.getDefault();
    when(category.getLocale()).thenReturn(expected);
    Locale actual = testling.getLocale();
    assertThat(actual).isSameAs(expected);
  }

  @Test
  public void testGetViewTypeName() throws Exception {
    assertThat(testling.getViewTypeName()).isNull();
  }
}
