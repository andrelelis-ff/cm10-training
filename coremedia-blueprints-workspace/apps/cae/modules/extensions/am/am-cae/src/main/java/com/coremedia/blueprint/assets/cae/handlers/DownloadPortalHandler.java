package com.coremedia.blueprint.assets.cae.handlers;

import com.coremedia.blueprint.assets.cae.AMLocalizationKeys;
import com.coremedia.blueprint.assets.cae.AssetDetails;
import com.coremedia.blueprint.assets.cae.CategoryOverview;
import com.coremedia.blueprint.assets.cae.DownloadCollectionOverview;
import com.coremedia.blueprint.assets.cae.DownloadPortal;
import com.coremedia.blueprint.assets.cae.DownloadPortalContext;
import com.coremedia.blueprint.assets.cae.DownloadPortalFactory;
import com.coremedia.blueprint.assets.cae.Notification;
import com.coremedia.blueprint.assets.cae.PaginatedAssets;
import com.coremedia.blueprint.assets.cae.SearchOverview;
import com.coremedia.blueprint.assets.cae.TaxonomyOverview;
import com.coremedia.blueprint.assets.contentbeans.AMAsset;
import com.coremedia.blueprint.assets.contentbeans.AMAssetRendition;
import com.coremedia.blueprint.assets.contentbeans.AMTaxonomy;
import com.coremedia.blueprint.cae.view.HashBasedFragmentHandler;
import com.coremedia.blueprint.cae.web.links.NavigationLinkSupport;
import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.view.ViewUtils;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.links.Link;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.base.links.UriConstants.Segments.PREFIX_DYNAMIC;
import static com.coremedia.blueprint.base.links.UriConstants.Segments.SEGMENTS_FRAGMENT;

/**
 * Handles all Download Portal related request and link processing.
 */
@Link
@RequestMapping
public class DownloadPortalHandler {

  private static final Logger LOG = LoggerFactory.getLogger(DownloadPortalHandler.class);

  // Also update blueprint-tests/test-extensions/corporate-tests/corporate-cae-wrapper/src/main/java/com/coremedia/blueprint/uitesting/corporate/wrapper/base/Navigation.java
  private static final String PREFIX_DOWNLOAD_PORTAL = "asset-download-portal";

  private static final String PORTAL = "portal";

  private static final String PAGINATED_ASSETS = "category-assets";

  private static final String CONTEXT_ID = "contextId";

  private static final String SITE = "site";

  private static final String DOWNLOAD_COLLECTION = "download-collection";

  private static final String DOWNLOAD_COLLECTION_OVERVIEW = "download-collection-overview";

  private static final String DC_PREPARE = "prepare";

  private static final String DC_DOWNLOAD = "download";

  private static final String DOWNLOAD_COLLECTION_DATA = "download-collection-data";

  static final String DOWNLOAD_PORTAL_ERROR_VIEW = "asDownloadPortalError";

  static final String ASSETS_VIEW = "_assets";

  static final String DOWNLOAD_COLLECTION_PREPARE_VIEW = "_download-collection-prepare";

  static final String DOWNLOAD_COLLECTION_DOWNLOAD_VIEW = "_download-collection-download";

  static final String DOWNLOAD_COLLECTION_OVERVIEW_VIEW = "_download-collection-overview";

  static final String DOWNLOAD_COLLECTION_ZIP_FILENAME = "asset-download-collection.zip";

  /**
   * URI pattern, for URIs like "/dynamic/fragment/corporate/am-download-portal"
   * <p>
   * Note: "site" segment needs to be available so that the {@link com.coremedia.blueprint.cae.filter.SiteFilter} can
   * properly determine the site
   * </p>
   */
  private static final String DYNAMIC_PORTAL_PREFIX =
          "/" + PREFIX_DYNAMIC +
                  "/" + SEGMENTS_FRAGMENT +
                  "/{" + SITE + "}" +
                  "/" + PREFIX_DOWNLOAD_PORTAL;

  private static final String DYNAMIC_PATTERN_PORTAL =
          DYNAMIC_PORTAL_PREFIX +
                  "/" + PORTAL +
                  "/{" + CONTEXT_ID + "}";

  private static final String DYNAMIC_PATTERN_PAGINATED_ASSETS =
          DYNAMIC_PORTAL_PREFIX +
                  "/" + PAGINATED_ASSETS +
                  "/{" + CONTEXT_ID + "}";

  private static final String PATTERN_DOWNLOAD_COLLECTION_PREPARE =
          "/{" + SITE + "}" +
                  "/" + PREFIX_DOWNLOAD_PORTAL +
                  "/" + DOWNLOAD_COLLECTION +
                  "/" + DC_PREPARE;

  private static final String PATTERN_DOWNLOAD_COLLECTION_DOWNLOAD =
          "/{" + SITE + "}" +
                  "/" + PREFIX_DOWNLOAD_PORTAL +
                  "/" + DOWNLOAD_COLLECTION +
                  "/" + DC_DOWNLOAD;

  private static final String PATTERN_DOWNLOAD_COLLECTION_OVERVIEW =
          DYNAMIC_PORTAL_PREFIX +
                  "/" + DOWNLOAD_COLLECTION_OVERVIEW +
                  "/{" + CONTEXT_ID + "}";

  public static final String CATEGORY_REQUEST_PARAMETER_NAME = "category";
  public static final String ASSET_REQUEST_PARAMETER_NAME = "asset";
  public static final String PAGE_REQUEST_PARAMETER_NAME = "page";
  public static final String SUBJECT_REQUEST_PARAMETER_NAME = "subject";
  public static final String SEARCH_REQUEST_PARAMETER_NAME = "search";
  public static final String DOWNLOAD_COLLECTION_REQUEST_PARAMETER_NAME = "download-collection";

  private DownloadPortalFactory downloadPortalFactory;

  private ContextHelper contextHelper;

  private ValidationService<ContentBean> validationService;

  private ContentRepository contentRepository;

  private ContentBeanFactory contentBeanFactory;

  @Required
  public void setDownloadPortalFactory(DownloadPortalFactory downloadPortalFactory) {
    this.downloadPortalFactory = downloadPortalFactory;
  }

  @Required
  public void setContextHelper(ContextHelper contextHelper) {
    this.contextHelper = contextHelper;
  }

  @Required
  public void setValidationService(ValidationService<ContentBean> validationService) {
    this.validationService = validationService;
  }

  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  @Required
  public void setContentBeanFactory(ContentBeanFactory contentBeanFactory) {
    this.contentBeanFactory = contentBeanFactory;
  }


  // ---------------------- handling requests ---------------------------------------------------------------------

  /**
   * Resolves links to the category overview and asset detail pages. Specific categories and assets are passed
   * as query parameters.
   *
   * @param navigation the navigation context of the download portal
   * @param categoryId the id of {@link AMTaxonomy} that represents the given category
   * @param assetBean  the {@link AMAsset bean} that represents the given asset
   * @param assetId    the id of {@link AMAsset} that represents the given asset
   * @return the ModelAndView
   */
  @GetMapping(value = DYNAMIC_PATTERN_PORTAL)
  public ModelAndView handleDownloadPortalRequest(@PathVariable(CONTEXT_ID) CMChannel navigation,
                                                  @RequestParam(value = CATEGORY_REQUEST_PARAMETER_NAME, required = false) AMTaxonomy categoryBean,
                                                  @RequestParam(value = CATEGORY_REQUEST_PARAMETER_NAME, required = false) String categoryId,
                                                  @RequestParam(value = ASSET_REQUEST_PARAMETER_NAME, required = false) AMAsset assetBean,
                                                  @RequestParam(value = ASSET_REQUEST_PARAMETER_NAME, required = false) String assetId) {
    ModelAndView modelAndView;
    // we are passing the id AND the bean to be able to distinguish between requests with invalid ids and requests
    // without ids
    if (isInvalidBean(categoryId, categoryBean) || isInvalidBean(assetId, assetBean)) {
      modelAndView = getDownloadPortalError(navigation);
    } else {
      if (null == assetId && null == assetBean) {
        // category overview request
        final CategoryOverview categoryOverview = downloadPortalFactory.createCategoryOverview(categoryBean);
        modelAndView = getModelAndViewWithNavigation(categoryOverview, navigation, null);
      } else {
        // if this is a deep link from the search or from the preview, the category is null
        AssetDetails assetDetails = downloadPortalFactory.createAssetDetails(navigation, assetBean, categoryBean);
        if (null != assetDetails) {
          modelAndView = getModelAndViewWithNavigation(assetDetails, navigation, null);
        } else {
          modelAndView = getDownloadPortalError(navigation);
        }
      }
    }

    return modelAndView;
  }

  /**
   * Resolves links to the subject taxonomy overview page. The specific taxonomy is passed as query parameter.
   *
   * @param navigation  the navigation context of the download portal
   * @param subjectBean the {@link CMTaxonomy} that represents the subject
   * @return the ModelAndView
   */
  @GetMapping(value = DYNAMIC_PATTERN_PORTAL, params = SUBJECT_REQUEST_PARAMETER_NAME)
  public ModelAndView handleSubjectAssetsRequest(@PathVariable(CONTEXT_ID) CMChannel navigation,
                                                 @RequestParam(value = SUBJECT_REQUEST_PARAMETER_NAME) CMTaxonomy subjectBean) {
    if (null != subjectBean && subjectBean.getContent().isInProduction()) {
      TaxonomyOverview subjectOverview = downloadPortalFactory.createSubjectOverview(subjectBean);
      return getModelAndViewWithNavigation(subjectOverview, navigation, null);
    } else {
      return getDownloadPortalError(navigation);
    }
  }

  /**
   * Resolves links to the subject taxonomy overview page. The specific taxonomy is passed as query parameter.
   *
   * @param navigation  the navigation context of the download portal
   * @param query the query string to use
   * @return the ModelAndView
   */
  @GetMapping(value = DYNAMIC_PATTERN_PORTAL, params = SEARCH_REQUEST_PARAMETER_NAME)
  public ModelAndView handleSearchRequest(@PathVariable(CONTEXT_ID) CMChannel navigation,
                                          @RequestParam(value = SEARCH_REQUEST_PARAMETER_NAME) String query) {
    // query is a required request param, so it cannot be null
    SearchOverview searchOverview = downloadPortalFactory.createSearchOverview(query);
    return getModelAndViewWithNavigation(searchOverview, navigation, null);
  }

  /**
   * Resolves links to the asset collection overview page.
   *
   * @param navigation  the navigation context of the download portal
   * @return the ModelAndView
   */
  @GetMapping(value = DYNAMIC_PATTERN_PORTAL, params = DOWNLOAD_COLLECTION_REQUEST_PARAMETER_NAME)
  public ModelAndView handleDownloadCollectionOverviewRequest(@PathVariable(CONTEXT_ID) CMChannel navigation) {
    DownloadCollectionOverview downloadCollectionOverview = downloadPortalFactory.createDownloadCollectionOverview(Collections.emptyList());
    return getModelAndViewWithNavigation(downloadCollectionOverview, navigation, null);
  }

  /**
   * Resolves links to assets on a category overview page. The specific category is passed as query parameter.
   *
   * @param navigation   the navigation context of the download portal
   * @param categoryBean the {@link AMTaxonomy} that represents the category
   * @param page         the page number to render (optional, defaults to 1)
   * @param response     the servlet response
   * @return the ModelAndView
   */
  @GetMapping(value = DYNAMIC_PATTERN_PAGINATED_ASSETS)
  public ModelAndView handlePaginatedCategoryAssetsRequest(@PathVariable(CONTEXT_ID) CMChannel navigation,
                                                           @RequestParam(value = CATEGORY_REQUEST_PARAMETER_NAME, required = false) AMTaxonomy categoryBean,
                                                           @RequestParam(value = PAGE_REQUEST_PARAMETER_NAME, defaultValue = "1") Integer page,
                                                           HttpServletResponse response) {
    PaginatedAssets paginatedCategoryAssets = downloadPortalFactory.createPaginatedCategoryAssets(
            categoryBean, navigation, page);
    if (null != categoryBean) {
      paginatedCategoryAssets.setBaseRequestParams(ImmutableMap.of(CATEGORY_REQUEST_PARAMETER_NAME, Integer.toString(categoryBean.getContentId())));
    }

    return handlePaginatedAssetsRequest(page, paginatedCategoryAssets, navigation, response);
  }

  /**
   * Resolves links to assets on a subject taxonomy overview page. The specific taxonomy is passed as query parameter.
   *
   * @param navigation  the navigation context of the download portal
   * @param subjectBean the {@link CMTaxonomy} that represents the subject
   * @param page        the page number to render (optional, defaults to 1)
   * @param response    the servlet response
   * @return the ModelAndView
   */
  @GetMapping(value = DYNAMIC_PATTERN_PAGINATED_ASSETS, params = SUBJECT_REQUEST_PARAMETER_NAME)
  public ModelAndView handlePaginatedSubjectAssetsRequest(@PathVariable(CONTEXT_ID) CMChannel navigation,
                                                          @RequestParam(value = SUBJECT_REQUEST_PARAMETER_NAME) CMTaxonomy subjectBean,
                                                          @RequestParam(value = PAGE_REQUEST_PARAMETER_NAME, defaultValue = "1") Integer page,
                                                          HttpServletResponse response) {
    PaginatedAssets paginatedSubjectAssets = downloadPortalFactory.createPaginatedSubjectAssets(
            subjectBean, navigation, page);
    paginatedSubjectAssets.setBaseRequestParams(ImmutableMap.of(SUBJECT_REQUEST_PARAMETER_NAME, Integer.toString(subjectBean.getContentId())));

    return handlePaginatedAssetsRequest(page, paginatedSubjectAssets, navigation, response);
  }

  /**
   * Resolves links to assets on a subject taxonomy overview page. The specific taxonomy is passed as query parameter.
   *
   * @param navigation  the navigation context of the download portal
   * @param query the query string to use
   * @param page        the page number to render (optional, defaults to 1)
   * @param response    the servlet response
   * @return the ModelAndView
   */
  @GetMapping(value = DYNAMIC_PATTERN_PAGINATED_ASSETS, params = SEARCH_REQUEST_PARAMETER_NAME)
  public ModelAndView handlePaginatedSearchAssetsRequest(@PathVariable(CONTEXT_ID) CMChannel navigation,
                                                         @RequestParam(value = SEARCH_REQUEST_PARAMETER_NAME) String query,
                                                         @RequestParam(value = PAGE_REQUEST_PARAMETER_NAME, defaultValue = "1") Integer page,
                                                         HttpServletResponse response) {

    PaginatedAssets paginatedSearchAssets;
    if (isSearchQueryTooShort(query)) {
      paginatedSearchAssets = downloadPortalFactory.createEmptyPaginatedAssets();
      Notification notification = new Notification(
              Notification.NotificationType.WARNING,
              AMLocalizationKeys.SEARCH_NOTIFICATION_QUERY_TOO_SHORT,
              ImmutableList.of(query.trim())
      );
      paginatedSearchAssets.setNotification(notification);
    } else {
      paginatedSearchAssets = downloadPortalFactory.createPaginatedSearchAssets(query, navigation, page);
      addSearchNotification(paginatedSearchAssets, query);
    }
    paginatedSearchAssets.setBaseRequestParams(ImmutableMap.of(SEARCH_REQUEST_PARAMETER_NAME, query));
    return handlePaginatedAssetsRequest(page, paginatedSearchAssets, navigation, response);
  }

  /**
   * Prepares the download of multiple selected renditions as a ZIP file.
   * The response body will contain the URL from where to download the file.
   *
   * @param downloadCollectionRawData the download collection data as a raw JSON String
   * @param response                  the servlet response
   * @throws IOException
   */
  @PostMapping(value = PATTERN_DOWNLOAD_COLLECTION_PREPARE)
  public void prepareDownloadingCollection(@RequestParam(value = DOWNLOAD_COLLECTION_DATA, required = true) String downloadCollectionRawData,
                                           HttpServletResponse response) throws IOException {

    Map<String, List<String>> downloadCollectionMap = getDownloadCollectionMap(downloadCollectionRawData);
    if (downloadCollectionMap == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      String message = "Prepare download request for asset collection => No valid data given";
      LOG.warn(message);
      IOUtils.write(message, response.getOutputStream());
    } else {

      List<AMAssetRendition> renditionsToDownload = getRenditionsToDownload(downloadCollectionMap);
      try {
        downloadPortalFactory.prepareRenditionsDownload(renditionsToDownload);
        response.setStatus(HttpServletResponse.SC_OK);
      } catch (Exception e) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        IOUtils.write("Failed to zip download collection data", response.getOutputStream());
        LOG.error("Failed to zip download collection data", e);
      }
    }
  }

  /**
   * Downloads the ZIP file containing multiple renditions that is prepared
   * in {@link #prepareDownloadingCollection(String, HttpServletResponse)}.
   *
   * @param downloadCollectionRawData the download collection data as a raw JSON String
   * @param response               the Servlet response
   * @throws IOException
   */
  @PostMapping(value = PATTERN_DOWNLOAD_COLLECTION_DOWNLOAD)
  public void downloadRenditionCollection(@RequestParam(value = DOWNLOAD_COLLECTION_DATA, required = true) String downloadCollectionRawData,
                                          HttpServletResponse response) throws IOException {

    Map<String, List<String>> downloadCollectionMap = getDownloadCollectionMap(downloadCollectionRawData);

    if (downloadCollectionMap == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      String message = "Download request for asset Collection => No valid data given";
      LOG.warn(message);
      IOUtils.write(message, response.getOutputStream());
    } else {

      response.setContentType("application/zip");
      response.addHeader("Content-Disposition", "attachment; filename=\"" + DOWNLOAD_COLLECTION_ZIP_FILENAME + "\"");

      List<AMAssetRendition> renditionsToDownload = getRenditionsToDownload(downloadCollectionMap);
      File downloadFile = downloadPortalFactory.getPreparedDownload(renditionsToDownload);
      if (null != downloadFile ) {
        FileInputStream fis = new FileInputStream(downloadFile);
        try {
          IOUtils.copy(fis, response.getOutputStream());
        } finally {
          IOUtils.closeQuietly(fis);
        }
        response.setStatus(HttpServletResponse.SC_OK);
      } else {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        LOG.error("ZIP file not found rendition collection: " + downloadCollectionRawData);
      }
    }
  }

  @PostMapping(value = PATTERN_DOWNLOAD_COLLECTION_OVERVIEW)
  public ModelAndView getDownloadCollectionOverview(@RequestParam(value = DOWNLOAD_COLLECTION_DATA, required = true) String downloadCollectionRawData,
                                                    @PathVariable(CONTEXT_ID) CMChannel navigation) throws IOException {

    Map<String, List<String>> downloadCollectionMap = getDownloadCollectionMap(downloadCollectionRawData);

    DownloadCollectionOverview downloadCollectionOverview = downloadPortalFactory.createDownloadCollectionOverview(getRenditionsToDownload(downloadCollectionMap));

    return getModelAndViewWithNavigation(downloadCollectionOverview, navigation, "collection");
  }
  // ---------------------- building links ---------------------------------------------------------------------

  /**
   * Creates links to the root {@link com.coremedia.blueprint.assets.cae.CategoryOverview}. Links to a specific categories and assets within the Download
   * Portal contain the context information as query parameters. Currently these parameters are appended
   * by the corresponding AJAX methods in the frontend.
   *
   * @param uriTemplate the uri template of the link
   * @return the link to the download portal
   */
  @SuppressWarnings("UnusedDeclaration")
  @Link(type = DownloadPortal.class, uri = DYNAMIC_PATTERN_PORTAL)
  public UriComponents buildAmDownloadPortalLink(UriTemplate uriTemplate) {
    Navigation navigation = contextHelper.currentSiteContext();
    return getUriComponentsBuilder(uriTemplate, navigation).build();
  }

  @SuppressWarnings("UnusedDeclaration")
  @Link(type = DownloadPortalContext.class, view = DOWNLOAD_COLLECTION_PREPARE_VIEW, uri = PATTERN_DOWNLOAD_COLLECTION_PREPARE)
  public UriComponents buildDownloadCollectionPrepareLink(UriTemplate uriTemplate) {
    Navigation navigation = contextHelper.currentSiteContext();
    return getUriComponentsBuilder(uriTemplate, navigation).build();
  }

  @SuppressWarnings("UnusedDeclaration")
  @Link(type = DownloadPortalContext.class, view = DOWNLOAD_COLLECTION_DOWNLOAD_VIEW, uri = PATTERN_DOWNLOAD_COLLECTION_DOWNLOAD)
  public UriComponents buildDownloadCollectionDownloadLink(UriTemplate uriTemplate) {
    Navigation navigation = contextHelper.currentSiteContext();
    return getUriComponentsBuilder(uriTemplate, navigation).build();
  }

  @SuppressWarnings("UnusedDeclaration")
  @Link(type = CategoryOverview.class, view = ASSETS_VIEW, uri = DYNAMIC_PATTERN_PAGINATED_ASSETS)
  public UriComponents buildAmPaginatedCategoryAssetsLink(UriTemplate uriTemplate) {
    Navigation navigation = contextHelper.currentSiteContext();
    return getUriComponentsBuilder(uriTemplate, navigation).build();
  }

  @SuppressWarnings("UnusedDeclaration")
  @Link(type = SearchOverview.class, view = ASSETS_VIEW, uri = DYNAMIC_PATTERN_PAGINATED_ASSETS)
  public UriComponents buildAmPaginatedSearchAssetsLink(UriTemplate uriTemplate) {
    Navigation navigation = contextHelper.currentSiteContext();
    return getUriComponentsBuilder(uriTemplate, navigation).build();
  }

  @SuppressWarnings("UnusedDeclaration")
  @Link(type = TaxonomyOverview.class, view = ASSETS_VIEW, uri = DYNAMIC_PATTERN_PAGINATED_ASSETS)
  public UriComponents buildAmPaginatedSubjectAssetsLink(UriTemplate uriTemplate) {
    Navigation navigation = contextHelper.currentSiteContext();
    return getUriComponentsBuilder(uriTemplate, navigation).build();
  }

  @SuppressWarnings("UnusedDeclaration")
  @Link(type = DownloadCollectionOverview.class, view = DOWNLOAD_COLLECTION_OVERVIEW_VIEW, uri = PATTERN_DOWNLOAD_COLLECTION_OVERVIEW)
  public UriComponents buildDownloadCollectionOverviewLink(UriTemplate uriTemplate) {
    Navigation navigation = contextHelper.currentSiteContext();
    return getUriComponentsBuilder(uriTemplate, navigation).build();
  }

  private ModelAndView getModelAndViewWithNavigation(@NonNull Object bean,
                                                     @NonNull Navigation navigation,
                                                     @Nullable String view) {
    ModelAndView modelAndView = HandlerHelper.createModelWithView(bean, StringUtils.defaultString(view, ViewUtils.DEFAULT_VIEW));
    // adding the navigation to the ModelAndView is a Blueprint prerequisite for rendering
    NavigationLinkSupport.setNavigation(modelAndView, navigation);
    return modelAndView;
  }

  private ModelAndView getDownloadPortalError(Navigation navigation) {
    ModelAndView modelAndView;
    modelAndView = HandlerHelper.notFound();
    modelAndView.setViewName(DOWNLOAD_PORTAL_ERROR_VIEW);
    NavigationLinkSupport.setNavigation(modelAndView, navigation);
    return modelAndView;
  }

  private UriComponentsBuilder getUriComponentsBuilder(UriTemplate uriTemplate, Navigation navigation) {
    CMContext navigationContext = null;
    if (null != navigation) {
      navigationContext = navigation.getContext();
    }
    if (null == navigationContext) {
      throw new IllegalArgumentException("Cannot resolve 'navigation' context when building link for navigation " + navigation);
    }
    int nearestNavigationId = navigationContext.getContentId();
    String rootNavigationSegment = navigationContext.getRootNavigation().getSegment();

    URI uri = uriTemplate.expand(rootNavigationSegment, nearestNavigationId);
    return UriComponentsBuilder.fromUri(uri);
  }

  /**
   * Check whether the given arguments are invalid and an error page should be generated.
   *
   * @param beanId optional bean id
   * @param bean   optional bean matching the given id.
   * @return false if the given bean exists for a given non null id and in case it does whether it has valid content
   */
  private boolean isInvalidBean(String beanId, ContentBean bean) {
    return beanId != null && bean == null || bean != null && !validationService.validate(bean) || bean != null && !bean.getContent().isInProduction();
  }

  private void addModifiedParamsHeader(HttpServletResponse response, String paramName, String modifiedValue) {
    response.addHeader(HashBasedFragmentHandler.MODIFIED_PARAMETERS_HEADER_PREFIX + paramName, modifiedValue);
  }

  private ModelAndView handlePaginatedAssetsRequest(int requestedPageNo, PaginatedAssets paginatedAssets, CMChannel navigation, HttpServletResponse response) {
    int actualPageNo = paginatedAssets.getCurrentPage();
    if (requestedPageNo != actualPageNo) {
      addModifiedParamsHeader(response, PAGE_REQUEST_PARAMETER_NAME, Integer.toString(actualPageNo));
    }

    return getModelAndViewWithNavigation(paginatedAssets, navigation, null);
  }

  private static boolean isSearchQueryTooShort(@NonNull String query) {
    return query.trim().length() < 3;
  }

  private void addSearchNotification(@NonNull PaginatedAssets paginatedAssets, @NonNull String query) {

    Notification notification;
    if (paginatedAssets.getTotalCount() == 0) {
      notification = new Notification(
              Notification.NotificationType.WARNING,
              AMLocalizationKeys.SEARCH_NOTIFICATION_NO_RESULTS,
              ImmutableList.of(query)
      );
    } else if (paginatedAssets.getTotalCount() == 1) {
      notification = new Notification(
              Notification.NotificationType.SUCCESS,
              AMLocalizationKeys.SEARCH_NOTIFICATION_ONE_RESULT,
              ImmutableList.of(query)
      );
    } else {
      notification = new Notification(
              Notification.NotificationType.SUCCESS,
              AMLocalizationKeys.SEARCH_NOTIFICATION_RESULTS,
              ImmutableList.of(paginatedAssets.getTotalCount(), query)
      );
    }
    paginatedAssets.setNotification(notification);
  }

  // ---------------------- Download ZIP file ---------------------------------------------------------------------

  /**
   * Retrieves the renditions to download from a download collection map linking asset IDs to rendition names.
   *
   * @param downloadCollectionMap the download collection data map
   * @return the renditions to download.
   */
  protected List<AMAssetRendition> getRenditionsToDownload(Map<String, List<String>> downloadCollectionMap) {

    List<AMAssetRendition> renditionsToDownload = new ArrayList<>();

    for (Map.Entry<String, List<String>> downloadCollectionMapEntry : downloadCollectionMap.entrySet()) {
      String assetId = downloadCollectionMapEntry.getKey();
      Content assetContent = contentRepository.getContent(assetId);
      if (null != assetContent) {
        getValidRenditionsForDownload(renditionsToDownload, downloadCollectionMapEntry.getValue(), assetContent);
      } else {
        LOG.info("Skipping unknown asset with id {}", assetId);
      }
    }
    return renditionsToDownload;
  }

  private void getValidRenditionsForDownload(@NonNull List<AMAssetRendition> renditionsToDownload,
                                             @NonNull List<String> renditionNames,
                                             @NonNull Content assetContent) {
    if (isAssetValid(assetContent)) {
      AMAsset amAsset = contentBeanFactory.createBeanFor(assetContent, AMAsset.class);
      if (amAsset != null) {
        addValidRenditions(renditionsToDownload, renditionNames, amAsset);
      } else {
        throw new NullPointerException("Could not determine AMAsset based on asset content");
      }
    }
  }

  private static void addValidRenditions(@NonNull List<AMAssetRendition> renditionsToDownload,
                                  @NonNull List<String> renditionNames,
                                  @NonNull AMAsset amAsset) {
    List<AMAssetRendition> publishedRenditions = amAsset.getPublishedRenditions();

    for (String renditionName : renditionNames) {
      AMAssetRendition amAssetRendition = getRenditionForName(publishedRenditions, renditionName);
      if (amAssetRendition != null) {
        renditionsToDownload.add(amAssetRendition);
      } else {
        LOG.info("Download Request for Rendition Collection => No published AMAssetRendition with name {} for asset with id {}",
                renditionName, amAsset.getContentId());
      }
    }
  }

  private static boolean isAssetValid(@NonNull Content assetContent) {
    if (assetContent.getType() == null || !assetContent.getType().isSubtypeOf(AMAsset.NAME)) {
      LOG.info("Download Request for Rendition Collection => No Asset for id: " + assetContent.getId());
      return false;
    }

    if (assetContent.isDeleted() || assetContent.isDestroyed()) {
      LOG.info("Download Request for Rendition Collection => Asset is deleted/destroyed");
      return false;
    }
    return true;
  }

  /**
   * Parses a download collection map (asset ID --> list of names for renditions to download)
   * from raw data (JSON String).
   *
   * @param downloadCollectionRawData the raw data as a JSON String
   * @return the download collection map
   */
  protected static Map<String, List<String>> getDownloadCollectionMap(String downloadCollectionRawData) {

    Map<String, List<String>> downloadCollectionMap = null;

    try {
      ObjectMapper mapper = new ObjectMapper();
      downloadCollectionMap = mapper.readValue(downloadCollectionRawData, new TypeReference<Map<String, List<String>>>() {
      });
    } catch (Exception e) {
      LOG.error("Failed to extract download collection data", e);
    }

    return downloadCollectionMap;
  }

  /**
   * Get {@link AMAssetRendition} for given rendition name.
   *
   * @param renditions    {@link List} List of {@link AMAssetRendition}s
   * @param renditionName String Name of rendition
   * @return {@link AMAssetRendition}
   */
  protected static AMAssetRendition getRenditionForName(List<AMAssetRendition> renditions, String renditionName) {
    for (AMAssetRendition amAssetRendition : renditions) {
      if (amAssetRendition.getName().equals(renditionName)) {
        return amAssetRendition;
      }
    }
    return null;
  }

}
