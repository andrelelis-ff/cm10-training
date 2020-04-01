package com.coremedia.livecontext.tree;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionInitializer;
import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper;
import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.livecontext.ecommerce.augmentation.AugmentationService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.common.CommerceBeanFactory;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * The live context tree relation resolves content of type CMExternalChannel
 * and applies the matching category subtree and resolves matching contents of type CMExternalChannel
 * <p/>
 * The tree relation only support bottom up lookups.
 * Since CMExternalChannel does not reference any contents as children #getChildrenOf is not implemented.
 */
public class ExternalChannelContentTreeRelation implements TreeRelation<Content> {
  private static final Logger LOG = LoggerFactory.getLogger(ExternalChannelContentTreeRelation.class);

  //local variables to avoid contentbean dependency
  private static final String EXTERNAL_ID = "externalId";
  private static final String CM_EXTERNAL_CHANNEL = "CMExternalChannel";

  private AugmentationService augmentationService;
  private CommerceTreeRelation commerceTreeRelation;
  private SitesService sitesService;
  private CommerceConnectionInitializer commerceConnectionInitializer;

  @Override
  public Collection<Content> getChildrenOf(Content parent) {
    throw new UnsupportedOperationException(
            ExternalChannelContentTreeRelation.class.getName() + " only supports bottum up lookups.");
  }

  @Override
  public Content getParentOf(Content child) {
    if (!isApplicable(child)) {
      return null;
    }

    Site site = sitesService.getContentSiteAspect(child).getSite();
    if (site == null) {
      LOG.warn("Content '{}' has no site, cannot determine parent content.", child.getPath());
      return null;
    }

    Optional<CommerceId> childCommerceId = getCommerceIdFrom(child);
    if (!childCommerceId.isPresent()) {
      return null;
    }

    Optional<CommerceConnection> commerceConnectionOpt = commerceConnectionInitializer.findConnectionForSite(site);

    if (!commerceConnectionOpt.isPresent()) {
      LOG.debug("Commerce connection is not available for site '{}'; not looking up parent content.",
              site.getName());
      return null;
    }

    Optional<Category> childCategoryOpt = findCategoryFor(child);
    if (!childCategoryOpt.isPresent()) {
      return null;
    }

    Category childCategory = childCategoryOpt.get();

    return getParentOf(childCategory, child, site);
  }

  @Nullable
  private Content getParentOf(@NonNull Category childCategory, @NonNull Content child, @NonNull Site site) {
    Category parentCategory = commerceTreeRelation.getParentOf(childCategory);
    if (parentCategory != null) {
      return getParentContent(parentCategory, site);
    }

    if (childCategory.isRoot()) {
      // avoid infinite loop when called with site root document
      Content siteRoot = site.getSiteRootDocument();
      if (!child.equals(siteRoot)) {
        return siteRoot;
      }
    }

    return null;
  }

  @Nullable
  private Content getParentContent(@NonNull Category parentCategory, @NonNull Site site) {
    Content parentContent = getNearestContentForCategory(parentCategory, site);
    if (parentContent == null) {
      return site.getSiteRootDocument();
    }
    return parentContent;
  }

  @Nullable
  public Content getNearestContentForCategory(@Nullable Category category, @Nullable Site site) {
    if (category == null || site == null) {
      return null;
    }

    Content augmentingContent = null;
    if (augmentationService != null) {
      augmentingContent = augmentationService.getContent(category);
    }

    if (null != augmentingContent) {
      return augmentingContent;
    }

    Category parentCategory = commerceTreeRelation.getParentOf(category);
    if (null != parentCategory) {
      return getNearestContentForCategory(parentCategory, site);
    }

    return null;
  }

  @Override
  public Content getParentUnchecked(Content child) {
    return getParentOf(child);
  }

  @Override
  public List<Content> pathToRoot(Content child) {
    List<Content> path = new ArrayList<>();
    Content parent = child;
    while (parent != null) {
      path.add(parent);
      parent = getParentOf(parent);
    }
    Collections.reverse(path);
    LOG.trace("path to root for {}: {}", child, path);
    return path;
  }

  @Override
  public boolean isRoot(Content item) {
    return getParentOf(item) == null;
  }

  @Override
  public boolean isApplicable(Content item) {
    return item != null && item.getType().isSubtypeOf(CM_EXTERNAL_CHANNEL) && isLinkedCategoryValid(item);
  }

  private boolean isLinkedCategoryValid(@NonNull Content item) {
    return findCategoryFor(item).isPresent();
  }

  @NonNull
  private static Optional<CommerceId> getCommerceIdFrom(@NonNull Content content) {
    String reference = content.getString(EXTERNAL_ID);
    return CommerceIdParserHelper.parseCommerceId(reference);
  }

  @NonNull
  private Optional<Category> findCategoryFor(@NonNull Content content) {
    Optional<CommerceConnection> optConnection = getSiteForContent(content)
            .flatMap(site -> commerceConnectionInitializer.findConnectionForSite(site));

    if (!optConnection.isPresent()) {
      return Optional.empty();
    }

    return getCommerceIdFrom(content)
            .flatMap(commerceId -> findCategoryFor(optConnection.get(), commerceId));
  }

  @NonNull
  private Optional<Category> findCategoryFor(@NonNull CommerceConnection connection, @NonNull CommerceId commerceId) {
    StoreContext storeContext = requireNonNull(connection.getStoreContext(), "store context not available");
    CommerceBeanFactory commerceBeanFactory = connection.getCommerceBeanFactory();

    return Optional.ofNullable((Category) commerceBeanFactory.loadBeanFor(commerceId, storeContext));
  }

  @NonNull
  private Optional<Site> getSiteForContent(@NonNull Content item) {
    return sitesService.getContentSiteAspect(item).findSite();
  }

  @Autowired(required = false)
  @Qualifier("categoryAugmentationService")
  public void setAugmentationService(AugmentationService augmentationService) {
    this.augmentationService = augmentationService;
  }

  @Autowired
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }

  @Autowired
  public void setCommerceConnectionInitializer(CommerceConnectionInitializer commerceConnectionInitializer) {
    this.commerceConnectionInitializer = commerceConnectionInitializer;
  }

  @Autowired
  public void setCommerceTreeRelation(CommerceTreeRelation commerceTreeRelation) {
    this.commerceTreeRelation = commerceTreeRelation;
  }
}
