package com.coremedia.livecontext.ecommerce.ibm.catalog;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.cap.content.Content;
import com.coremedia.livecontext.ecommerce.asset.CatalogPicture;
import com.coremedia.livecontext.ecommerce.catalog.Catalog;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogName;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.CommerceObject;
import com.coremedia.livecontext.ecommerce.common.NotFoundException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractIbmCommerceBean;
import com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper;
import com.coremedia.livecontext.ecommerce.ibm.common.DataMapTransformationHelper;
import com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper;
import com.coremedia.livecontext.ecommerce.ibm.link.WcsUrlProvider;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.xml.Markup;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService.DEFAULT_CATALOG_ALIAS;
import static com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper.findString;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_9_0;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class CategoryImpl extends AbstractIbmCommerceBean implements Category, CommerceObject {

  private static final Logger LOG = LoggerFactory.getLogger(CategoryImpl.class);

  public static final String ROOT_CATEGORY_ROLE_ID = "ROOT";

  private static final String EMPTY_URL = "http://ci.vm";

  private Map<String, Object> delegate;
  private WcCatalogWrapperService catalogWrapperService;
  private WcsUrlProvider wcsUrlProvider;

  protected Map<String, Object> getDelegate() {
    if (delegate == null) {
      if (isRoot()) {
        return Collections.emptyMap();
      }
      delegate = getDelegateFromCache();
      if (delegate == null) {
        throw new NotFoundException(getId() + " (category not found in catalog)");
      }
    }
    return delegate;
  }

  /**
   * Perform by-id-call to get detail data
   *
   * @return detail data map
   */
  Map<String, Object> getDelegateFromCache() {
    UserContext userContext = CurrentUserContext.get();
    CommerceCache commerceCache = getCommerceCache();

    CategoryCacheKey cacheKey = new CategoryCacheKey(getId(), getContext(), userContext, getCatalogWrapperService(),
            commerceCache);

    return commerceCache.get(cacheKey);
  }

  @Override
  public void load() {
    getDelegate();
  }

  @Override
  public void setDelegate(Object delegate) {
    this.delegate = (Map<String, Object>) delegate;
  }

  public WcCatalogWrapperService getCatalogWrapperService() {
    return catalogWrapperService;
  }

  @Required
  public void setCatalogWrapperService(WcCatalogWrapperService catalogWrapperService) {
    this.catalogWrapperService = catalogWrapperService;
  }

  @Required
  public void setWcsUrlProvider(WcsUrlProvider wcsUrlProvider) {
    this.wcsUrlProvider = wcsUrlProvider;
  }

  @Override
  public String getExternalId() {
    if (isRoot()) {
      return ROOT_CATEGORY_ROLE_ID;
    }

    return getStringValueFromDelegate("identifier");
  }

  @Override
  public String getExternalTechId() {
    return getStringValueFromDelegate("uniqueID");
  }

  @Override
  public String getName() {
    if (isRoot()) {
      return ROOT_CATEGORY_ROLE_ID;
    }

    return getStringValueFromDelegate("name");
  }

  @Override
  public Markup getShortDescription() {
    String shortDescription = getStringValueFromDelegate("shortDescription");
    return toRichtext(shortDescription);
  }

  @Override
  public Markup getLongDescription() {
    String longDescription = getStringValueFromDelegate("longDescription");
    return toRichtext(longDescription, false);
  }

  @Override
  public String getThumbnailUrl() {
    return findString(getDelegate(), "thumbnail")
            .flatMap(thumbnailUri -> getLinkService().getImageUrl(thumbnailUri, getContext()))
            .orElse(null);
  }

  @Override
  public String getDefaultImageUrl() {
    return findString(getDelegate(), "fullImage")
            .flatMap(defaultImageUri -> getLinkService().getImageUrl(defaultImageUri, getContext()))
            .orElse(null);
  }

  /**
   * Returns a "ready-to-use" storefront url that points to the shop category page.
   * Normally (when the commerce adapter supports it) it would come from the commerce adapter/system.
   * Until that happens we simulate this by computing this value for ourselves.
   * Todo: change this implementation to read the url from delegate or use a template or whatsoever
   */
  @Override
  public String getStorefrontUrl() {
    return wcsUrlProvider.buildCategoryLink(this, emptyList(), false)
            .map(UriComponentsBuilder::toUriString)
            .orElse(EMPTY_URL);
  }

  @Override
  @NonNull
  public List<Category> getChildren() {
    if (isRoot()) {
      CatalogAlias catalogAlias = getId().getCatalogAlias();
      return getCatalogService().findTopCategories(catalogAlias, getContext());
    }
    return getCatalogService().findSubCategories(this);
  }

  @Override
  @NonNull
  public List<Product> getProducts() {
    return getCatalogService().findProductsByCategory(this);
  }

  @Override
  @Nullable
  public Category getParent() {
    if (isRoot()) {
      return null;
    }

    StoreContext context = getContext();
    CatalogAlias catalogAlias = getCatalogAlias();

    String catalogId = getCatalog(this).map(Catalog::getExternalId).orElse(null);
    List<String> parentCategoryIds = DataMapTransformationHelper.getParentCatGroupIdForSingleWrapper(getDelegate(),
            catalogId);

    return parentCategoryIds.stream()
            .filter(categoryId -> !isNullOrEmpty(categoryId) && !"-1".equals(categoryId))
            .map(this::findCategoryOrLog)
            .filter(Objects::nonNull)
            .findFirst()
            .orElseGet(() -> getCatalogService().findRootCategory(catalogAlias, context));
  }

  @Nullable
  private Category findCategoryOrLog(@NonNull String categoryId) {
    CommerceId commerceId = getCommerceIdProvider().formatCategoryTechId(getCatalogAlias(), categoryId);
    Category category = getCatalogService().findCategoryById(commerceId, getContext());
    if (category == null) {
      LOG.debug("Category '{}' points to an invalid parent category: {}", this.getId(), commerceId);
      return null;
    }
    return category;
  }

  @Override
  @NonNull
  public List<Category> getBreadcrumb() {
    List<Category> result = new ArrayList<>();

    Category parent = getParent();
    if (parent != null) {
      result.addAll(parent.getBreadcrumb());
    }

    if (!isRoot()) {
      result.add(this);
    }

    return result;
  }

  @Nullable
  @Override
  public String getSeoSegment() {
    return SeoSegmentHelper.getSeoSegment(getDelegate(), getContext());
  }

  @Override
  public String getMetaDescription() {
    return getStringValueFromDelegate("metaDescription");
  }

  @Override
  public String getMetaKeywords() {
    return getStringValueFromDelegate("metaKeyword");
  }

  @Override
  public String getTitle() {
    return getStringValueFromDelegate("title");
  }

  @NonNull
  @Override
  public String getDisplayName() {
    if (!isRoot()) {
      return getExternalId();
    }

    Optional<Catalog> catalog = getCatalog(this);
    Optional<String> catalogName = catalog.map(Catalog::getName).map(CatalogName::value);

    return withDefaultCatalogName(catalogName.orElseGet(this::getExternalId));
  }

  @NonNull
  private String withDefaultCatalogName(@NonNull String catalogName) {
    return DEFAULT_CATALOG_ALIAS.equals(getCatalogAlias()) ? catalogName + " (Default)" : catalogName;
  }

  @Nullable
  @Override
  public CatalogPicture getCatalogPicture() {
    return findAssetService()
            .map(assetService -> assetService.getCatalogPicture(getDefaultImageUrl(), getReference()))
            .orElseGet(() -> new CatalogPicture("#", null));
  }

  @Nullable
  @Override
  public Content getPicture() {
    return getPictures().stream().findFirst().orElse(null);
  }

  @NonNull
  @Override
  public List<Content> getPictures() {
    return findAssetService()
            .map(assetService -> assetService.findPictures(getReference()))
            .orElseGet(Collections::emptyList);
  }

  @NonNull
  @Override
  public List<Content> getVisuals() {
    return findAssetService()
            .map(assetService -> assetService.findVisuals(getReference()))
            .orElseGet(Collections::emptyList);
  }

  @NonNull
  @Override
  public List<Content> getDownloads() {
    return findAssetService()
            .map(assetService -> assetService.findDownloads(getReference()))
            .orElseGet(Collections::emptyList);
  }

  @Override
  public boolean isRoot() {
    return isRootCategoryId(getId());
  }

  static boolean isRootCategoryId(@NonNull CommerceId id) {
    return id.getExternalId().map(ROOT_CATEGORY_ROLE_ID::equals).orElse(false);
  }

  @Nullable
  private String getStringValueFromDelegate(@NonNull String key) {
    return findString(getDelegate(), key).orElse(null);
  }
}
