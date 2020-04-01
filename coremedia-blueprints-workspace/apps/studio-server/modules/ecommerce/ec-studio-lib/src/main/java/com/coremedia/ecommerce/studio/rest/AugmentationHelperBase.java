package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractCommerceBean;
import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGridService;
import com.coremedia.blueprint.base.pagegrid.PageGridContentKeywords;
import com.coremedia.cap.common.CapStructHelper;
import com.coremedia.cap.common.DuplicateNameException;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.Catalog;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import com.coremedia.rest.cap.intercept.InterceptService;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.cap.struct.StructBuilderMode.LOOSE;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.joining;

abstract class AugmentationHelperBase<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AugmentationHelperBase.class);

  static final String DEFAULT_BASE_FOLDER_NAME = "Augmentation";

  static final String EXTERNAL_ID = "externalId";
  public static final String OTHER_CATALOGS_FOLDER_NAME = "_other_catalogs";

  protected ContentRepository contentRepository;
  protected AugmentationService augmentationService;
  protected ContentBackedPageGridService pageGridService;
  private InterceptService interceptService;
  protected SitesService sitesService;

  private String baseFolderName;
  protected ContentType contentType;

  @Nullable
  abstract Content augment(@NonNull T type);

  @Nullable
  protected Content createContent(@NonNull Content parent, @NonNull String name, @NonNull Map<String, Object> properties) {
    // Create content (taking possible interceptors into consideration)
    ContentWriteRequest writeRequest = null;
    if (interceptService != null) {
      writeRequest = interceptService.interceptCreate(parent, name, contentType, properties);
      interceptService.handleErrorIssues(writeRequest);
    }

    Content content = parent.getChild(name);
    if (content != null) {
      return content;
    }

    try {
      Map<String, Object> myProperties = writeRequest != null ? writeRequest.getProperties() : properties;
      content = contentType.create(parent, name.trim(), myProperties);
    } catch (DuplicateNameException e) {
      LOGGER.debug("Ignored concurrent (redundant) augmentation request", e);
      content = parent.getChild(name);
    } catch (Throwable t){
      LOGGER.error("An error occured while augmenting category", t);
      throw t;
    }

    // will most likely be non-null but maybe we're facing a concurrent content deletion
    if (content == null) {
      return null;
    }

    if (interceptService != null) {
      interceptService.postProcess(content, null);
    }

    return content;
  }

  @Nullable
  protected String computeFolderPath(@NonNull CommerceBean commerceBean) {
    Category category = getCategoryForCommerceBean(commerceBean);

    Site site = sitesService.getSite(category.getContext().getSiteId());
    if (site == null) {
      return null;
    }

    String rootPath = site.getSiteRootFolder().getPath();

    List<String> subPathsToJoin = newArrayList(rootPath, baseFolderName);

    //Each catalog needs a separate folder. If not default catalog use the catalog alias as the basefolder.
    Optional<Catalog> catalog = getCatalog(category);

    //when catalog is empty then we assume that there is only the default catalog
    boolean isDefaultCatalog = catalog.map(Catalog::isDefaultCatalog).orElse(true);
    if (!isDefaultCatalog) {
      CatalogAlias catalogAlias = category.getId().getCatalogAlias();
      subPathsToJoin.add(OTHER_CATALOGS_FOLDER_NAME);
      subPathsToJoin.add(catalogAlias.value());
    }

    for (Category breadcrumbCategory : category.getBreadcrumb()) {
      subPathsToJoin.add(getEscapedDisplayName(breadcrumbCategory));
    }

    return subPathsToJoin.stream().collect(joining("/"));
  }

  @VisibleForTesting
  @NonNull
  Optional<Catalog> getCatalog(Category category) {
    return AbstractCommerceBean.getCatalog(category);
  }

  @NonNull
  protected static String getEscapedDisplayName(@NonNull Category category) {
    // External ids of category can contain '/'. See CMS-5075
    return category.getDisplayName().replace('/', '_');
  }

  @Nullable
  abstract Content getCategoryContent(@NonNull Category category);

  @NonNull
  protected Category getRootCategory(@NonNull CommerceBean commerceBean) {
    Category currentCategory = getCategoryForCommerceBean(commerceBean);

    while (!currentCategory.isRoot()) {
      Category parent = currentCategory.getParent();
      if (parent == null) {
        LOGGER.warn("Root category '{}' is not properly recognized.", currentCategory.getId());
        return currentCategory;
      }

      currentCategory = parent;
    }

    return currentCategory;
  }

  @NonNull
  private static Category getCategoryForCommerceBean(@NonNull CommerceBean commerceBean) {
    if (commerceBean instanceof Category) {
      return (Category) commerceBean;
    } else if (commerceBean instanceof Product) {
      return ((Product) commerceBean).getCategory();
    } else {
      throw new IllegalArgumentException("Unsupported commerce bean type.");
    }
  }

  @Nullable
  protected static Content getLayoutSettings(@NonNull Content content, @NonNull String pageGridName) {
    Struct placementStruct = CapStructHelper.getStruct(content, pageGridName);
    if (placementStruct == null) {
      return null;
    }

    Struct placements2Struct = CapStructHelper.getStruct(placementStruct, PageGridContentKeywords.PLACEMENTS_PROPERTY_NAME);
    if (placements2Struct == null) {
      return null;
    }

    return (Content) placements2Struct.get(PageGridContentKeywords.LAYOUT_PROPERTY_NAME);
  }

  private Struct createEmptyStruct() {
    return contentRepository.getConnection().getStructService().createStructBuilder().build();
  }

  protected Struct createStructWithLayoutLink(Content layoutSettings) {
    Struct pageGridStruct = createEmptyStruct();

    StructBuilder builder = pageGridStruct.builder().mode(LOOSE);
    builder.set(PageGridContentKeywords.PLACEMENTS_PROPERTY_NAME, createEmptyStruct());
    builder.enter(PageGridContentKeywords.PLACEMENTS_PROPERTY_NAME);
    builder.declareLink(PageGridContentKeywords.LAYOUT_PROPERTY_NAME,
            contentRepository.getContentType("Document_"), layoutSettings);
    builder.add(PageGridContentKeywords.PLACEMENTS_PLACEMENTS_PROPERTY_NAME, createEmptyStruct());
    return builder.build();
  }

  @Autowired
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  @Autowired(required = false)
  public void setInterceptService(InterceptService interceptService) {
    this.interceptService = interceptService;
  }

  @Autowired
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }

  @Value("${livecontext.augmentation.path:" + DEFAULT_BASE_FOLDER_NAME + "}")
  public void setBaseFolderName(String baseFolderName) {
    this.baseFolderName = baseFolderName;
  }

  @Autowired
  public void setPageGridService(ContentBackedPageGridService pageGridService) {
    this.pageGridService = pageGridService;
  }

}
