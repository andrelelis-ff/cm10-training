package com.coremedia.livecontext.navigation;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.ContentSiteAspect;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.contentbeans.LiveContextExternalChannelImpl;
import com.coremedia.livecontext.context.LiveContextNavigation;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.InvalidContextException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.common.StoreContextProvider;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LiveContextNavigationFactoryTest {

  @InjectMocks
  private LiveContextNavigationFactory testling;

  @Mock
  private CatalogService catalogService;

  @Mock
  private StoreContext storeContext;

  @Mock
  private StoreContextProvider storeContextProvider;

  @Mock
  private LiveContextNavigationTreeRelation treeRelation;

  @Mock
  private SitesService sitesService;

  @Mock
  private Site site;

  @Mock
  private Content content;

  @Mock
  private LiveContextExternalChannelImpl externalChannel;

  @Mock
  private ContentSiteAspect contentSiteAspect;

  @Mock
  private CommerceConnection connection;

  @Mock
  private AugmentationService augmentationService;

  @Mock
  private ContentBeanFactory contentBeanFactory;

  @Mock
  private ValidationService validationService;

  @Before
  public void setUp() {
    when(storeContext.getConnection()).thenReturn(connection);

    when(connection.getStoreContextProvider()).thenReturn(storeContextProvider);
    when(connection.getCatalogService()).thenReturn(catalogService);

    when(sitesService.getContentSiteAspect(content)).thenReturn(contentSiteAspect);
    when(contentSiteAspect.findSite()).thenReturn(Optional.ofNullable(site));

    when(contentBeanFactory.createBeanFor(content, LiveContextNavigation.class)).thenReturn(externalChannel);
    when(validationService.validate(externalChannel)).thenReturn(true);

    CurrentStoreContext.set(storeContext);
  }

  @After
  public void teardown() {
    CurrentStoreContext.remove();
  }

  @Test
  public void testCreateNavigationWithValidCategory() {
    Category categoryToCreateFrom = mock(Category.class);

    LiveContextNavigation actual = testling.createNavigation(categoryToCreateFrom, site);
    assertThat(actual).as("The returned Navigation must not be null").isNotNull();

    Category categoryInNavigation = actual.getCategory();
    assertThat(categoryInNavigation)
            .as("The created LiveContextNavigation is expected to contain the category given by the first parameter of this method")
            .isSameAs(categoryToCreateFrom);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateNavigationWithCategoryIsNull() {
    testling.createNavigation(null, site);
  }

  @Test
  public void testCreateNavigationWithAugmentingContent() {
    Category categoryToCreateFrom = mock(Category.class);
    when(augmentationService.getContent(categoryToCreateFrom)).thenReturn(content);

    LiveContextNavigation actual = testling.createNavigation(categoryToCreateFrom, site);
    assertThat(actual).as("The returned Navigation must not be null").isNotNull();
    assertThat(actual).isInstanceOf(LiveContextExternalChannelImpl.class);
  }

  @Test
  public void testCreateNavigationBySeoSegment() {
    String existingSeoSegment = "existingSeoSegment";
    StoreContext storeContext = mock(StoreContext.class);
    Category category = mock(Category.class);

    when(storeContextProvider.findContextByContent(content)).thenReturn(Optional.of(storeContext));
    when(catalogService.findCategoryBySeoSegment(existingSeoSegment, storeContext)).thenReturn(category);

    LiveContextNavigation actual = testling.createNavigationBySeoSegment(content, existingSeoSegment);
    assertThat(actual).as("The returned Navigation must not be null").isNotNull();
    assertThat(actual.getCategory())
            .as("The created LiveContextNavigation is expected to contain the category given by the first parameter of this method")
            .isSameAs(category);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateNavigationBySeoSegmentContentWithoutContext() {
    Content content = mock(Content.class);
    String existingSeoSegment = "existingSeoSegment";

    when(storeContextProvider.findContextByContent(content)).thenReturn(Optional.empty());

    testling.createNavigationBySeoSegment(content, existingSeoSegment);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateNavigationBySeoSegmentNoValidSeoSegment() {
    Content content = mock(Content.class);
    String notExistingSeoSegment = "notExistingSeoSegment";
    StoreContext storeContext = mock(StoreContext.class);

    when(storeContextProvider.findContextByContent(content)).thenReturn(Optional.of(storeContext));
    when(catalogService.findCategoryBySeoSegment(notExistingSeoSegment, storeContext)).thenReturn(null);

    testling.createNavigationBySeoSegment(content, notExistingSeoSegment);
  }

  @Test(expected = InvalidContextException.class)
  public void testCreateNavigationBySeoSegmentInvalidContextException() {
    Content invalidContent = mock(Content.class);
    String anySeo = "anySeo";
    when(storeContextProvider.findContextByContent(invalidContent)).thenThrow(InvalidContextException.class);
    testling.createNavigationBySeoSegment(invalidContent, anySeo);
  }

  @Test(expected = CommerceException.class)
  public void testCreateNavigationBySeoSegmentCommerceException() {
    Content content = mock(Content.class);
    String anySeoSegment = "anySeoSegment";
    StoreContext storeContext = mock(StoreContext.class);

    when(storeContextProvider.findContextByContent(content)).thenReturn(Optional.of(storeContext));
    when(catalogService.findCategoryBySeoSegment(anySeoSegment, storeContext)).thenThrow(CommerceException.class);

    testling.createNavigationBySeoSegment(content, anySeoSegment);
  }
}
