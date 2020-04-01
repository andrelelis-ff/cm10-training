package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.MappedCatalogsProvider;
import com.coremedia.blueprint.base.pagegrid.PageGridContentKeywords;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.struct.Struct;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper.format;
import static java.util.Objects.requireNonNull;

/**
 * A REST service to augment a category.
 */
@Named
class CategoryAugmentationHelper extends AugmentationHelperBase<Category> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CategoryAugmentationHelper.class);

  private static final String CM_EXTERNAL_CHANNEL = "CMExternalChannel";

  static final String CATEGORY_PAGEGRID_STRUCT_PROPERTY = PageGridContentKeywords.PAGE_GRID_STRUCT_PROPERTY;
  public static final String CATEGORY_PRODUCT_PAGEGRID_STRUCT_PROPERTY = "pdpPagegrid";

  static final String TITLE = "title";
  static final String SEGMENT = "segment";

  private MappedCatalogsProvider mappedCatalogsProvider;

  @Override
  @Nullable
  Content augment(@NonNull Category category) {
    // create folder hierarchy for category
    Content categoryFolder = contentRepository.createSubfolders(computeFolderPath(category));

    if (categoryFolder == null) {
      return null;
    }

    Map<String, Object> properties = buildCategoryContentDocumentProperties(category);

    if (augmentationService != null) {
      initializeLayoutSettings(category, properties);
    }

    return createContent(categoryFolder, computeDocumentName(category), properties);
  }

  @NonNull
  private static String computeDocumentName(@NonNull Category category) {
    return (getEscapedDisplayName(category) + " (" + category.getExternalId() + ")")
            .replace('/', '_');
  }

  @VisibleForTesting
  void initializeLayoutSettings(@NonNull Category category, @NonNull Map<String, Object> properties) {
    Category rootCategory = getRootCategory(category);
    Content rootCategoryContent = getCategoryContent(rootCategory);

    if (rootCategoryContent == null) {
      if (!category.isRoot()) {
        //throw ecxeption if current category is not a root category
        // and if root category of current catalog is not augmented yet
        String msg = "Root category is not augmented (requested category is ' " + category.getId() +
                "') , cannot set default layouts.";
        LOGGER.warn(msg);
        throw new CommerceAugmentationException(msg);
      } else {
        //initialize root category content for current catalog
        initializeRootCategoryContent(category, properties);
      }
    } else {
      //initialize with current catalogs augmented root category
      initializeCategoryLayout(rootCategoryContent, rootCategory, category, properties);
      initializeProductLayout(rootCategoryContent, rootCategory, category, properties);
    }
  }

  @VisibleForTesting
  void initializeRootCategoryContent(@NonNull Category category, @NonNull Map<String, Object> properties) {
    Optional<Content> otherAugmentedRootCategory = lookupAugmentedRootCategoryInOtherCatalogs(category);
    if (otherAugmentedRootCategory.isPresent()) {
      initializeCategoryLayout(otherAugmentedRootCategory.get(), category, category, properties);
      initializeProductLayout(otherAugmentedRootCategory.get(), category, category, properties);
    } else {
      initializeLayoutSettingsWithSiteRoot(category, properties);
    }
  }

  private void initializeLayoutSettingsWithSiteRoot(@NonNull Category category, @NonNull Map<String, Object> properties) {
    Site site = requireNonNull(sitesService.getSite(category.getContext().getSiteId()));
    Content siteRootDocument = site.getSiteRootDocument();
    if (siteRootDocument != null) {
      Content layout = pageGridService.getLayout(siteRootDocument, PageGridContentKeywords.PAGE_GRID_STRUCT_PROPERTY);
      LOGGER.info("Getting default layout from site root document '{}' results to '{}'", siteRootDocument, layout);
      Struct structWithLayoutLink = createStructWithLayoutLink(layout);
      properties.put(CATEGORY_PAGEGRID_STRUCT_PROPERTY, structWithLayoutLink);
      properties.put(CATEGORY_PRODUCT_PAGEGRID_STRUCT_PROPERTY, structWithLayoutLink);
    }
  }

  @VisibleForTesting
  Optional<Content> lookupAugmentedRootCategoryInOtherCatalogs(@NonNull Category category) {
    //lookup in other catalogs
    LOGGER.debug("Root category of '{}' is not augmented. " +
            "Trying to find augmented root category in neigbour catalogs", category.getId());


    // try to find another root category, maybe in another catalog
    StoreContext context = category.getContext();
    List<Category> configuredRootCategories =
            mappedCatalogsProvider.getConfiguredRootCategories(context);

    return configuredRootCategories.stream()
            .map(augmentationService::getContent)
            .filter(Objects::nonNull)
            .findFirst();
  }

  private void initializeCategoryLayout(@NonNull Content rootCategoryContent, @NonNull Category rootCategory,
                                        @NonNull CommerceBean commerceBean, @NonNull Map<String, Object> properties) {
    Content defaultCategoryLayoutSettings = getLayoutSettings(rootCategoryContent, CATEGORY_PAGEGRID_STRUCT_PROPERTY);

    if (defaultCategoryLayoutSettings == null) {
      LOGGER.warn("No default category page layout found for root category '{}', "
                      + "cannot initialize category page layout for augmented category '{}'.",
              rootCategory.getId(), commerceBean.getId());
      return;
    }

    Struct structWithLayoutLink = createStructWithLayoutLink(defaultCategoryLayoutSettings);
    properties.put(CATEGORY_PAGEGRID_STRUCT_PROPERTY, structWithLayoutLink);
  }

  private void initializeProductLayout(@NonNull Content rootCategoryContent, @NonNull Category rootCategory,
                                       @NonNull CommerceBean commerceBean, @NonNull Map<String, Object> properties) {
    Content defaultProductLayoutSettings = getLayoutSettings(rootCategoryContent, CATEGORY_PRODUCT_PAGEGRID_STRUCT_PROPERTY);

    if (defaultProductLayoutSettings == null) {
      LOGGER.warn("No default product page layout found for root category '{}', "
                      + "cannot initialize product page layout for augmented category '{}'.",
              rootCategory.getId(), commerceBean.getId());
      return;
    }

    Struct structWithLayoutLink = createStructWithLayoutLink(defaultProductLayoutSettings);
    properties.put(CATEGORY_PRODUCT_PAGEGRID_STRUCT_PROPERTY, structWithLayoutLink);
  }

  @Override
  protected Content getCategoryContent(@NonNull Category category) {
    return augmentationService.getContent(category);
  }

  /**
   * Builds properties for an <code>CMExternalChannel</code> document.
   */
  private Map<String, Object> buildCategoryContentDocumentProperties(@NonNull Category category) {
    Map<String, Object> properties = new HashMap<>();

    properties.put(EXTERNAL_ID, format(category.getId()));

    // Initialize title and segment with the display name instead of relying on
    // `ContentInitializer.initChannel` as the latter will initialize the title
    // with the escaped display name of the content.
    properties.put(TITLE, category.getDisplayName());
    properties.put(SEGMENT, category.getDisplayName());

    return properties;
  }

  @Autowired(required = false)
  @Qualifier("categoryAugmentationService")
  public void setAugmentationService(AugmentationService augmentationService) {
    this.augmentationService = augmentationService;
  }

  @Value("${livecontext.augmentation.category.type:" + CM_EXTERNAL_CHANNEL + "}")
  public void setAugmentedContentType(ContentType contentType) {
    this.contentType = contentType;
  }

  @Autowired
  public void setMappedCatalogsProvider(MappedCatalogsProvider mappedCatalogsProvider) {
    this.mappedCatalogsProvider = mappedCatalogsProvider;
  }
}
