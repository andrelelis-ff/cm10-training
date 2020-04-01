package com.coremedia.ecommerce.studio.rest;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.struct.Struct;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

import static com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper.format;
import static com.coremedia.ecommerce.studio.rest.CategoryAugmentationHelper.CATEGORY_PRODUCT_PAGEGRID_STRUCT_PROPERTY;

/**
 * A REST service to augment a product.
 */
@Named
public class ProductAugmentationHelper extends AugmentationHelperBase<Product> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CategoryAugmentationHelper.class);

  private static final String CM_EXTERNAL_PRODUCT = "CMExternalProduct";
  private static final String PAGEGRID_STRUCT_PROPERTY = "pdpPagegrid";

  static final String TITLE = "title";

  private AugmentationService categoryAugmentationService;

  @Override
  @Nullable
  Content augment(@NonNull Product product) {
    Category parentCategory = product.getCategory();

    // create folder hierarchy for category
    Content categoryFolder = contentRepository.createSubfolders(computeFolderPath(parentCategory));

    if (categoryFolder == null) {
      return null;
    }

    Map<String, Object> properties = buildProductContentDocumentProperties(product);

    if (augmentationService != null) {
      initializeLayoutSettings(product, properties);
    }

    return createContent(categoryFolder, computeDocumentName(product), properties);
  }

  @VisibleForTesting
  void initializeLayoutSettings(Product product, Map<String, Object> properties) {
    Category rootCategory = getRootCategory(product);
    Content rootCategoryContent = getCategoryContent(rootCategory);

    if (rootCategoryContent == null) {
      String msg= "Root category is not augmented (requested product is ' " + product.getId() +
              "') , cannot set default layouts.";
      LOGGER.warn(msg);
      throw new CommerceAugmentationException(msg);
    }

    Content defaultProductLayoutSettings = getLayoutSettings(rootCategoryContent, CATEGORY_PRODUCT_PAGEGRID_STRUCT_PROPERTY);

    if (defaultProductLayoutSettings == null) {
      LOGGER.warn("No default category page layout found for root category '{}', "
                      + "cannot initialize category page layout for augmented category '{}'.",
              rootCategory.getId(), product.getId());
      return;
    }

    Struct structWithLayoutLink = createStructWithLayoutLink(defaultProductLayoutSettings);
    properties.put(PAGEGRID_STRUCT_PROPERTY, structWithLayoutLink);
  }

  @Override
  Content getCategoryContent(@NonNull Category category) {
    return categoryAugmentationService.getContent(category);
  }

  /**
   * Builds properties for an <code>CMExternalProduct</code> document.
   */
  private Map<String, Object> buildProductContentDocumentProperties(@NonNull Product product) {
    Map<String, Object> properties = new HashMap<>();
    properties.put(EXTERNAL_ID, format(product.getId()));

    // Initialize title with the product name instead of relying on
    // `ContentInitializer.initChannel` as the latter will initialize the title
    // with the name of the content which is not intended
    properties.put(TITLE, product.getName());

    return properties;
  }

  @NonNull
  private static String computeDocumentName(@NonNull Product product) {
    return (product.getName() + " (" + product.getExternalId() + ")")
            .replace('/', '_');
  }

  @Autowired(required = false)
  @Qualifier("productAugmentationService")
  public void setAugmentationService(AugmentationService augmentationService) {
    this.augmentationService = augmentationService;
  }

  @Value("${livecontext.augmentation.product.type:" + CM_EXTERNAL_PRODUCT + "}")
  public void setAugmentedContentType(ContentType contentType) {
    this.contentType = contentType;
  }

  @Autowired(required = false)
  @Qualifier("categoryAugmentationService")
  public void setCategoryAugmentationService(AugmentationService categoryAugmentationService) {
    this.categoryAugmentationService = categoryAugmentationService;
  }
}
