package com.coremedia.blueprint.assets.cae.handlers;

import com.coremedia.blueprint.assets.cae.AMLocalizationKeys;
import com.coremedia.blueprint.assets.cae.AssetDetails;
import com.coremedia.blueprint.assets.cae.CategoryOverview;
import com.coremedia.blueprint.assets.cae.DownloadCollectionOverview;
import com.coremedia.blueprint.assets.cae.DownloadPortalFactory;
import com.coremedia.blueprint.assets.cae.Notification;
import com.coremedia.blueprint.assets.cae.PaginatedAssets;
import com.coremedia.blueprint.assets.cae.SearchOverview;
import com.coremedia.blueprint.assets.cae.TaxonomyOverview;
import com.coremedia.blueprint.assets.contentbeans.AMAsset;
import com.coremedia.blueprint.assets.contentbeans.AMAssetRendition;
import com.coremedia.blueprint.assets.contentbeans.AMTaxonomy;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.view.ViewUtils;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.HttpError;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.Collections;

import static com.coremedia.blueprint.assets.cae.handlers.DownloadPortalHandler.CATEGORY_REQUEST_PARAMETER_NAME;
import static com.coremedia.blueprint.assets.cae.handlers.DownloadPortalHandler.SUBJECT_REQUEST_PARAMETER_NAME;
import static com.google.common.collect.ImmutableMap.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DownloadPortalHandlerTest {

  private static final int ASSET_CONTENT_ID = 60001;
  private static final int CATEGORY_CONTENT_ID = 10001;
  private static final int CATEGORY_CONTENT_ID_2 = 10002;

  @InjectMocks
  private DownloadPortalHandler handler;

  @Mock
  private DownloadPortalFactory factory;

  @Mock
  private ContextHelper contextHelper;

  @Mock
  private CMContext cmContext;

  @Mock
  private CMNavigation cmNavigation;

  @Mock
  private CategoryOverview categoryOverview;

  @Mock
  private AssetDetails assetDetails;

  @Mock
  private CMChannel navigation;

  @Mock
  private Content taxonomyContent;

  @Mock
  private AMTaxonomy taxonomy;

  @Mock
  private Content taxonomyContent2;

  @Mock
  private AMTaxonomy taxonomy2;

  @Mock
  private SearchOverview searchOverview;

  @Mock
  private TaxonomyOverview taxonomyOverview;

  @Mock
  private DownloadCollectionOverview downloadCollectionOverview;

  @Mock
  private Content assetContent;

  @Mock
  private ContentType assetContentType;

  @Mock
  private AMAsset asset;

  @Mock
  private AMAssetRendition assetRendition;

  @Mock
  private PaginatedAssets paginatedAssets;

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private ContentBeanFactory contentBeanFactory;

  @Mock
  private ValidationService<ContentBean> validationService;

  @Mock
  private UriTemplate uriTemplate;

  private MockHttpServletResponse response = new MockHttpServletResponse();

  private int contentId = 123;

  @Before
  public void setUp() throws Exception {
    String segmentName = "corporate";

    when(taxonomy.getContentId()).thenReturn(CATEGORY_CONTENT_ID);
    when(taxonomy.getContent()).thenReturn(taxonomyContent);
    when(taxonomy.getContent().isInProduction()).thenReturn(true);

    when(taxonomy2.getContentId()).thenReturn(CATEGORY_CONTENT_ID_2);
    when(taxonomy2.getContent()).thenReturn(taxonomyContent2);
    when(taxonomy2.getContent().isInProduction()).thenReturn(true);

    when(asset.getContentId()).thenReturn(ASSET_CONTENT_ID);
    when(asset.getContent()).thenReturn(assetContent);
    when(asset.getContent().isInProduction()).thenReturn(true);
    when(assetContent.getType()).thenReturn(assetContentType);
    when(assetContentType.isSubtypeOf(AMAsset.NAME)).thenReturn(true);
    when(contentRepository.getContent(eq(ASSET_CONTENT_ID + ""))).thenReturn(assetContent);
    when(contentBeanFactory.createBeanFor(assetContent, AMAsset.class)).thenReturn(asset);

    when(factory.createCategoryOverview(null)).thenReturn(categoryOverview);
    when(factory.createCategoryOverview(taxonomy)).thenReturn(categoryOverview);
    when(factory.createAssetDetails(navigation, asset, taxonomy)).thenReturn(assetDetails);
    when(factory.createAssetDetails(navigation, asset, taxonomy2)).thenReturn(null);
    when(factory.createAssetDetails(navigation, asset, null)).thenReturn(assetDetails);
    when(validationService.validate(any(ContentBean.class))).thenReturn(true);

    when(contextHelper.currentSiteContext()).thenReturn(navigation);
    when(cmContext.getContentId()).thenReturn(contentId);
    when(cmNavigation.getSegment()).thenReturn(segmentName);
    when(cmContext.getRootNavigation()).thenReturn(cmNavigation);

    when(uriTemplate.expand(segmentName, contentId)).thenReturn(new URI(Integer.toString(contentId)));
  }

  @Test
  public void testHandleCategoryRootRequest() {
    final ModelAndView modelAndView = handler.handleDownloadPortalRequest(navigation, null, null, null, null);

    assertEquals(2, modelAndView.getModel().size());
    assertEquals(categoryOverview, HandlerHelper.getRootModel(modelAndView));
    assertEquals(navigation, modelAndView.getModel().get(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION));
    verify(factory).createCategoryOverview(null);
    verifyNoMoreInteractions(factory, categoryOverview, navigation);
  }

  @Test
  public void testHandleCategoryRequest() {
    final ModelAndView modelAndView = handler.handleDownloadPortalRequest(navigation, taxonomy, taxonomy.getContentId() + "", null, null);

    assertEquals(2, modelAndView.getModel().size());
    assertEquals(categoryOverview, HandlerHelper.getRootModel(modelAndView));
    assertEquals(navigation, modelAndView.getModel().get(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION));
    verify(factory).createCategoryOverview(taxonomy);
    verifyNoMoreInteractions(factory, categoryOverview, navigation);
  }

  @Test
  public void testHandleAssetInCategoryRequest() {
    final ModelAndView modelAndView = handler.handleDownloadPortalRequest(navigation, taxonomy, taxonomy.getContentId() + "", asset, asset.getContentId() + "");

    assertEquals(2, modelAndView.getModel().size());
    assertEquals(assetDetails, HandlerHelper.getRootModel(modelAndView));
    assertEquals(navigation, modelAndView.getModel().get(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION));
    verify(factory).createAssetDetails(navigation, asset, taxonomy);
    verifyNoMoreInteractions(factory, categoryOverview, navigation);
  }

  @Test
  public void testHandleAssetWithoutCategoryRequest() {
    final ModelAndView modelAndView = handler.handleDownloadPortalRequest(navigation, null, null, asset, asset.getContentId() + "");

    assertEquals(2, modelAndView.getModel().size());
    assertEquals(assetDetails, HandlerHelper.getRootModel(modelAndView));
    assertEquals(navigation, modelAndView.getModel().get(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION));
    verify(factory).createAssetDetails(navigation, asset, null);
    verifyNoMoreInteractions(factory, categoryOverview, navigation);
  }

  @Test
  public void testInvalidTaxonomyId() {
    final ModelAndView modelAndView = handler.handleDownloadPortalRequest(navigation, null, "abcdef", null, null);
    verifyErrorModelExists(modelAndView);
    verify(factory, never()).createAssetDetails(any(CMChannel.class), any(AMAsset.class), any(AMTaxonomy.class));
    verify(factory, never()).createCategoryOverview(any(AMTaxonomy.class));
  }

  @Test
  public void testInvalidAssetId() {
    final ModelAndView modelAndView = handler.handleDownloadPortalRequest(navigation, null, null, null, "abcdef");
    verifyErrorModelExists(modelAndView);
    verify(factory, never()).createAssetDetails(any(CMChannel.class), any(AMAsset.class), any(AMTaxonomy.class));
    verify(factory, never()).createCategoryOverview(any(AMTaxonomy.class));
  }

  @Test
  public void testHandleAssetWithInvalidCategoryRequest() {
    final ModelAndView modelAndView = handler.handleDownloadPortalRequest(navigation, taxonomy2, taxonomy2.getContentId() + "", asset, asset.getContentId() + "");
    verifyErrorModelExists(modelAndView);
    verify(factory).createAssetDetails(eq(navigation), eq(asset), eq(taxonomy2));
    verifyNoMoreInteractions(factory, categoryOverview, navigation);
  }

  @Test
  public void testBuildAmDownloadPortalLink() {
    when(navigation.getContext()).thenReturn(cmContext);

    final UriComponents uriComponents = handler.buildAmDownloadPortalLink(uriTemplate);
    assertEquals(Integer.toString(contentId), uriComponents.getPath());
  }

  @Test(expected=IllegalArgumentException.class)
  public void testBuildAmDownloadPortalLinkWithNavigationNull() {
    when(navigation.getContext()).thenReturn(null);

    // cannot handle navigation without context
    handler.buildAmDownloadPortalLink(uriTemplate);
  }

  @Test
  public void buildAmPaginatedCategoryAssetsLink() {
    when(navigation.getContext()).thenReturn(cmContext);

    final UriComponents uriComponents = handler.buildAmPaginatedCategoryAssetsLink(uriTemplate);
    assertEquals(Integer.toString(contentId), uriComponents.getPath());
  }

  @Test
  public void buildAmPaginatedSubjectAssetsLink() {
    when(navigation.getContext()).thenReturn(cmContext);


    final UriComponents uriComponents = handler.buildAmPaginatedSubjectAssetsLink(uriTemplate);
    assertEquals(Integer.toString(contentId), uriComponents.getPath());
  }


  @Test
  public void buildDownloadCollectionLink(){
    when(navigation.getContext()).thenReturn(cmContext);


    final UriComponents uriComponents = handler.buildDownloadCollectionDownloadLink(uriTemplate);
    assertEquals(Integer.toString(contentId), uriComponents.getPath());
  }

  @Test
  public void buildAmPaginatedSearchAssetsLink() {
    when(navigation.getContext()).thenReturn(cmContext);


    final UriComponents uriComponents = handler.buildAmPaginatedSearchAssetsLink(uriTemplate);
    assertEquals(Integer.toString(contentId), uriComponents.getPath());
  }

  @Test
  public void handleSearchAssetsRequest() {
    String searchString = "searchMe";
    when(factory.createSearchOverview(searchString)).thenReturn(searchOverview);

    ModelAndView modelAndView = handler.handleSearchRequest(navigation, searchString);

    assertEquals(2, modelAndView.getModel().size());
    assertEquals(searchOverview, modelAndView.getModel().get("self"));
    assertEquals(navigation, modelAndView.getModel().get(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION));
    assertEquals(ViewUtils.DEFAULT_VIEW, modelAndView.getViewName());

    verify(factory).createSearchOverview(searchString);
  }

  @Test
  public void handleSearchAssetsNoResultsRequest() {
    String searchString = "searchMe";
    when(factory.createSearchOverview(searchString)).thenReturn(searchOverview);

    ModelAndView modelAndView = handler.handleSearchRequest(navigation, searchString);

    assertEquals(2, modelAndView.getModel().size());
    assertEquals(searchOverview, modelAndView.getModel().get("self"));
    assertEquals(navigation, modelAndView.getModel().get(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION));
    assertEquals(ViewUtils.DEFAULT_VIEW, modelAndView.getViewName());

    verify(factory).createSearchOverview(searchString);
  }

  @Test
  public void handleSubjectAssetsRequest() {
    when(factory.createSubjectOverview(taxonomy)).thenReturn(taxonomyOverview);

    ModelAndView modelAndView = handler.handleSubjectAssetsRequest(navigation, taxonomy);
    assertEquals(2, modelAndView.getModel().size());
    assertEquals(taxonomyOverview, modelAndView.getModel().get("self"));
    assertEquals(navigation, modelAndView.getModel().get(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION));
    assertEquals(ViewUtils.DEFAULT_VIEW, modelAndView.getViewName());
    verify(factory).createSubjectOverview(taxonomy);
  }

  @Test
  public void handleSubjectAssetsRequestError() {
    ModelAndView modelAndView = handler.handleSubjectAssetsRequest(navigation, null);
    verifyErrorModelExists(modelAndView);
  }

  @Test
  public void buildDownloadCollectionPrepareLink() {
    when(navigation.getContext()).thenReturn(cmContext);

    final UriComponents uriComponents = handler.buildDownloadCollectionPrepareLink(uriTemplate);
    assertEquals(Integer.toString(contentId), uriComponents.getPath());
  }

  @Test
  public void buildDownloadCollectionDownloadLink() {
    when(navigation.getContext()).thenReturn(cmContext);

    final UriComponents uriComponents = handler.buildDownloadCollectionDownloadLink(uriTemplate);
    assertEquals(Integer.toString(contentId), uriComponents.getPath());
  }

  @Test
  public void buildDownloadCollectionOverviewLink() {
    when(navigation.getContext()).thenReturn(cmContext);

    final UriComponents uriComponents = handler.buildDownloadCollectionOverviewLink(uriTemplate);
    assertEquals(Integer.toString(contentId), uriComponents.getPath());
  }

  @Test
  public void handlePaginatedCategoryAssetsRequest() {
    when(factory.createPaginatedCategoryAssets(taxonomy, navigation, 2)).thenReturn(paginatedAssets);

    ModelAndView modelAndView = handler.handlePaginatedCategoryAssetsRequest(navigation, taxonomy, 2, response);
    assertEquals(2, modelAndView.getModel().size());
    assertEquals(paginatedAssets, modelAndView.getModel().get("self"));
    assertEquals(navigation, modelAndView.getModel().get(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION));
    assertEquals(ViewUtils.DEFAULT_VIEW, modelAndView.getViewName());
    verify(paginatedAssets).setBaseRequestParams(of(CATEGORY_REQUEST_PARAMETER_NAME, Integer.toString(taxonomy.getContentId())));
    verify(factory).createPaginatedCategoryAssets(taxonomy, navigation, 2);
  }

  @Test
  public void handlePaginatedSubjectAssetsRequest() {
    when(factory.createPaginatedSubjectAssets(taxonomy, navigation, 3)).thenReturn(paginatedAssets);

    ModelAndView modelAndView = handler.handlePaginatedSubjectAssetsRequest(navigation, taxonomy, 3, response);
    assertEquals(2, modelAndView.getModel().size());
    assertEquals(paginatedAssets, modelAndView.getModel().get("self"));
    assertEquals(navigation, modelAndView.getModel().get(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION));
    assertEquals(ViewUtils.DEFAULT_VIEW, modelAndView.getViewName());
    verify(paginatedAssets).setBaseRequestParams(of(SUBJECT_REQUEST_PARAMETER_NAME, Integer.toString(taxonomy.getContentId())));
    verify(factory).createPaginatedSubjectAssets(taxonomy, navigation, 3);
  }

  @Test
  public void testHandleSearchTooShortRequest() {

    when(factory.createEmptyPaginatedAssets()).thenReturn(paginatedAssets);
    String queryTooShort = "me";
    ArgumentCaptor<Notification> notificationArgumentCaptor = ArgumentCaptor.forClass(Notification.class);

    handler.handlePaginatedSearchAssetsRequest(navigation, queryTooShort, 1, response);

    verify(factory).createEmptyPaginatedAssets();
    verify(paginatedAssets).setBaseRequestParams(anyMap());
    verify(paginatedAssets).setNotification(notificationArgumentCaptor.capture());

    Notification notification = notificationArgumentCaptor.getValue();
    assertNotNull(notification);
    assertEquals(Notification.NotificationType.WARNING, notification.getType());
    assertEquals(AMLocalizationKeys.SEARCH_NOTIFICATION_QUERY_TOO_SHORT, notification.getKey());
  }

  @Test
  public void testHandleSearchRequest() {

    String searchString = "searchme";
    when(factory.createPaginatedSearchAssets(searchString, navigation, 1)).thenReturn(paginatedAssets);
    when(paginatedAssets.getTotalCount()).thenReturn(10L);
    ArgumentCaptor<Notification> notificationArgumentCaptor = ArgumentCaptor.forClass(Notification.class);

    handler.handlePaginatedSearchAssetsRequest(navigation, searchString, 1, response);

    verify(factory).createPaginatedSearchAssets(searchString, navigation, 1);
    verify(paginatedAssets).setBaseRequestParams(anyMap());
    verify(paginatedAssets).setNotification(notificationArgumentCaptor.capture());

    Notification notification = notificationArgumentCaptor.getValue();
    assertNotNull(notification);
    assertEquals(Notification.NotificationType.SUCCESS, notification.getType());
    assertEquals(AMLocalizationKeys.SEARCH_NOTIFICATION_RESULTS, notification.getKey());
  }

  @Test
  public void testHandleSearchRequestNoResult() {

    String searchString = "searchme";
    when(factory.createPaginatedSearchAssets(searchString, navigation, 1)).thenReturn(paginatedAssets);
    when(paginatedAssets.getTotalCount()).thenReturn(0L);
    ArgumentCaptor<Notification> notificationArgumentCaptor = ArgumentCaptor.forClass(Notification.class);

    handler.handlePaginatedSearchAssetsRequest(navigation, searchString, 1, response);

    verify(factory).createPaginatedSearchAssets(searchString, navigation, 1);
    verify(paginatedAssets).setBaseRequestParams(anyMap());

    verify(paginatedAssets).setNotification(notificationArgumentCaptor.capture());

    Notification notification = notificationArgumentCaptor.getValue();
    assertNotNull(notification);
    assertEquals(Notification.NotificationType.WARNING, notification.getType());
    assertEquals(AMLocalizationKeys.SEARCH_NOTIFICATION_NO_RESULTS, notification.getKey());  }

  @Test
  public void testHandleSearchRequestOneResult() {

    String searchString = "searchme";
    when(factory.createPaginatedSearchAssets(searchString, navigation, 1)).thenReturn(paginatedAssets);
    when(paginatedAssets.getTotalCount()).thenReturn(1L);
    ArgumentCaptor<Notification> notificationArgumentCaptor = ArgumentCaptor.forClass(Notification.class);

    handler.handlePaginatedSearchAssetsRequest(navigation, searchString, 1, response);

    verify(factory).createPaginatedSearchAssets(searchString, navigation, 1);
    verify(paginatedAssets).setBaseRequestParams(anyMap());

    verify(paginatedAssets).setNotification(notificationArgumentCaptor.capture());

    Notification notification = notificationArgumentCaptor.getValue();
    assertNotNull(notification);
    assertEquals(Notification.NotificationType.SUCCESS, notification.getType());
    assertEquals(AMLocalizationKeys.SEARCH_NOTIFICATION_ONE_RESULT, notification.getKey());
  }

  @Test
  public void testDownloadCollectionOverview() throws Exception {
    when(factory.createDownloadCollectionOverview(anyList())).thenReturn(downloadCollectionOverview);
    ImmutableList<AMAssetRendition> assetRenditions = ImmutableList.of(assetRendition);
    when(asset.getPublishedRenditions()).thenReturn(assetRenditions);
    when(assetRendition.getName()).thenReturn("original");

    ModelAndView modelAndView = handler.getDownloadCollectionOverview("{\"" + ASSET_CONTENT_ID + "\":[\"original\"]}", navigation);

    DownloadCollectionOverview result = (DownloadCollectionOverview) modelAndView.getModel().get("self");
    assertEquals(downloadCollectionOverview, result);

    verify(factory).createDownloadCollectionOverview(assetRenditions);
  }

  @Test
  public void testDownloadCollectionOverviewRequest() {
    when(factory.createDownloadCollectionOverview(anyList())).thenReturn(downloadCollectionOverview);

    handler.handleDownloadCollectionOverviewRequest(navigation);

    verify(factory).createDownloadCollectionOverview(Collections.emptyList());
  }

  private void verifyErrorModelExists(ModelAndView modelAndView) {
    assertEquals(2, modelAndView.getModel().size());
    assertTrue(HandlerHelper.getRootModel(modelAndView) instanceof HttpError);
    assertEquals(navigation, modelAndView.getModel().get(NavigationLinkSupport.ATTR_NAME_CMNAVIGATION));
  }
}
