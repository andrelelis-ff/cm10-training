package com.coremedia.livecontext.pagegrid;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.NoStoreContextAvailable;
import com.coremedia.blueprint.base.pagegrid.ContentBackedPageGridPlacement;
import com.coremedia.blueprint.base.pagegrid.impl.ContentBackedPageGridServiceImpl;
import com.coremedia.cap.content.Content;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Collection;
import java.util.Map;

/**
 * PageGridService merges content backed pageGrids along an external category hierarchy.
 */
public class ContentAugmentedPageGridServiceImpl extends ContentBackedPageGridServiceImpl<Content> {

  private static final Logger LOG = LoggerFactory.getLogger(ContentAugmentedPageGridServiceImpl.class);

  static final String CM_EXTERNAL_CHANNEL = "CMExternalChannel";

  private AugmentationService augmentationService;

  @NonNull
  @Override
  protected Map<String, ContentBackedPageGridPlacement> getMergedPageGridPlacements(
          @NonNull Content navigation, @NonNull String pageGridName,
          @NonNull Collection<? extends Content> layoutSections) {
    return getMergedHierarchicalPageGridPlacements(navigation, pageGridName, layoutSections);
  }

  /**
   * Make #getMergedHierarchicalPageGridPlacements available for
   * {@link ContentAugmentedProductPageGridServiceImpl#getMergedPageGridPlacements}
   */
  @NonNull
  Map<String, ContentBackedPageGridPlacement> getMergedHierarchicalPageGridPlacements(
          @NonNull Content navigation, @NonNull String pageGridName,
          @NonNull Collection<? extends Content> layoutSections) {
    return super.getMergedPageGridPlacements(navigation, pageGridName, layoutSections);
  }

  @Nullable
  @Override
  protected Content getParentOf(@Nullable Content content) {
    if (content == null || !content.getType().isSubtypeOf(CM_EXTERNAL_CHANNEL)) {
      return null;
    }

    return getTreeRelation().getParentOf(content);
  }

  @Nullable
  @Override
  public Content getLayout(@NonNull Content content, @NonNull String pageGridName) {
    Content style = styleSettingsDocument(content, pageGridName);
    if (style == null) {
      Content rootCategoryContent = getRootCategoryContent(content);
      if (rootCategoryContent != null) {
        style = styleSettingsDocument(rootCategoryContent, pageGridName);
      }
    }

    return style != null ? style : getDefaultLayout(content);
  }

  @Nullable
  private Content getRootCategoryContent(@NonNull Content content) {
    try {
      CommerceConnection commerceConnection = CurrentStoreContext.get().getConnection();

      StoreContext storeContext = getStoreContextForContent(content, commerceConnection);

      Category rootCategory = commerceConnection.getCatalogService()
              .findRootCategory(storeContext.getCatalogAlias(), storeContext);
      return augmentationService.getContent(rootCategory);
    } catch (CommerceException e) {
      LOG.warn("Could not retrieve root category for Content {}.", content, e);
      return null;
    }
  }

  @NonNull
  private StoreContext getStoreContextForContent(@NonNull Content content,
                                                 @NonNull CommerceConnection commerceConnection) {
    return commerceConnection.getStoreContextProvider()
            .findContextByContent(content)
            .orElseThrow(() -> new NoStoreContextAvailable(
                    "Store context could not be obtained for content with ID '" + content.getId() + "'."));
  }

  @Autowired
  @Qualifier("categoryAugmentationService")
  public void setAugmentationService(AugmentationService augmentationService) {
    this.augmentationService = augmentationService;
  }
}
