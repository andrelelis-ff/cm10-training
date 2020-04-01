package com.coremedia.livecontext.navigation;

import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.context.LiveContextNavigation;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.tree.CommerceTreeRelation;
import com.coremedia.livecontext.tree.ExternalChannelContentTreeRelation;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LiveContextNavigationTreeRelationTest {

  @Mock
  private ContentBeanFactory contentBeanFactory;

  @Mock
  private Category augmentedCategory;

  @Mock
  private CommerceTreeRelation treeRelation;

  @Mock
  private AugmentationService augmentationService;

  @Mock
  private Site site;

  @Mock
  private Content content;

  @InjectMocks
  @Spy
  private ExternalChannelContentTreeRelation delegate;

  @InjectMocks
  private LiveContextNavigationFactory navigationFactory;

  @InjectMocks
  private LiveContextNavigationTreeRelation testling;

  @Before
  public void setUp() {
    testling.setNavigationFactory(navigationFactory);
    testling.setDelegate(delegate);
    navigationFactory.setTreeRelation(testling);
    delegate.setCommerceTreeRelation(treeRelation);

    when(augmentationService.getContent(augmentedCategory)).thenReturn(content);
  }

  @Test
  public void testGetChildrenOf() {
    Category categoryChild1 = mock(Category.class);
    Category categoryChild2 = mock(Category.class);

    List<Category> categoryChildren = newArrayList(categoryChild1, categoryChild2);

    when(augmentedCategory.getChildren()).thenReturn(categoryChildren);

    LiveContextNavigation testNavigation = navigationFactory.createNavigation(augmentedCategory, site);
    Collection<Linkable> childrenOf = testling.getChildrenOf(testNavigation);
    assertThat(childrenOf).hasSize(2);

    Iterator<Linkable> iterator = childrenOf.iterator();
    LiveContextNavigation firstChild = (LiveContextNavigation) iterator.next();
    LiveContextNavigation secondChild = (LiveContextNavigation) iterator.next();
    assertThat(firstChild.getCategory()).isSameAs(categoryChild1);
    assertThat(secondChild.getCategory()).isSameAs(categoryChild2);
  }

  /**
   * The breadcrumb is the rendered result of
   * {@link LiveContextNavigationTreeRelation#pathToRoot(com.coremedia.blueprint.common.navigation.Linkable)}
   */
  @Test
  public void testBreadcrumb_CMS_5573() {
    Category c1 = mock(Category.class);
    Category c2 = mock(Category.class);
    Category c3 = mock(Category.class);
    when(c3.getBreadcrumb()).thenReturn(asList(augmentedCategory, c2, c1));

    LiveContextNavigation testNavigation = navigationFactory.createNavigation(c3, site);
    Collection<Linkable> breadcrumb = testling.pathToRoot(testNavigation);
    assertThat(breadcrumb).hasSize(4);
  }
}
