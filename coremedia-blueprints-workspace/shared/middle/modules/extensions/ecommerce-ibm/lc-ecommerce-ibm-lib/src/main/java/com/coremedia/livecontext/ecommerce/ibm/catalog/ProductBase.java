package com.coremedia.livecontext.ecommerce.ibm.catalog;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceCache;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentUserContext;
import com.coremedia.cap.content.Content;
import com.coremedia.livecontext.ecommerce.asset.CatalogPicture;
import com.coremedia.livecontext.ecommerce.catalog.Catalog;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.catalog.ProductAttribute;
import com.coremedia.livecontext.ecommerce.catalog.VariantFilter;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.CommerceObject;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractIbmCommerceBean;
import com.coremedia.livecontext.ecommerce.ibm.common.DataMapHelper;
import com.coremedia.livecontext.ecommerce.ibm.common.DataMapTransformationHelper;
import com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper;
import com.coremedia.livecontext.ecommerce.ibm.link.WcsUrlProvider;
import com.coremedia.livecontext.ecommerce.ibm.pricing.PersonalizedPriceByExternalIdCacheKey;
import com.coremedia.livecontext.ecommerce.ibm.pricing.StaticPricesByExternalIdCacheKey;
import com.coremedia.livecontext.ecommerce.ibm.pricing.WcPrice;
import com.coremedia.livecontext.ecommerce.ibm.pricing.WcPrices;
import com.coremedia.livecontext.ecommerce.ibm.user.UserContextHelper;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.xml.Markup;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Base class for product and product variant implementation.
 */
abstract class ProductBase extends AbstractIbmCommerceBean implements Product, CommerceObject {

  private static final Logger LOG = LoggerFactory.getLogger(ProductBase.class);

  private static final String EMPTY_URL = "http://pb.vm";
  private static final String USAGE_DEFINING = "Defining";

  protected Map<String, Object> delegate;

  private WcPrices priceInfo;
  private List<ProductAttribute> definingAttributes;
  private List<ProductAttribute> describingAttributes;

  private WcCatalogWrapperService catalogWrapperService;
  private WcsUrlProvider wcsUrlProvider;

  @NonNull
  protected abstract Map<String, Object> getDelegate();

  @Override
  public void setDelegate(Object delegate) {
    this.delegate = (Map<String, Object>) delegate;
  }

  @Nullable
  abstract Map<String, Object> getDelegateFromCache();

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

  protected WcPrices getPriceInfo() {
    if (priceInfo == null) {
      WcPrices wcPrices = new WcPrices();
      wcPrices.setDataMap(getDelegate());
      if (wcPrices.getPrices().size() > 1) {
        priceInfo = wcPrices;
      }

      if (priceInfo == null) {
        CatalogAlias catalogAlias = getId().getCatalogAlias();
        UserContext userContext = CurrentUserContext.get();
        CommerceCache commerceCache = getCommerceCache();

        StaticPricesByExternalIdCacheKey cacheKey = new StaticPricesByExternalIdCacheKey(getExternalId(), catalogAlias, getContext(),
                userContext, getCatalogWrapperService(), commerceCache);

        priceInfo = commerceCache.get(cacheKey);
      }
    }

    return priceInfo;
  }

  @Override
  public String getExternalId() {
    return getStringValue(getDelegate(), "partNumber");
  }

  @Override
  public String getExternalTechId() {
    return getStringValue(getDelegate(), "uniqueID");
  }

  @Override
  public String getName() {
    return getStringValue(getDelegate(), "name");
  }

  @Override
  public Markup getShortDescription() {
    String shortDescription = getStringValue(getDelegate(), "shortDescription");
    //short description by WCS is pure text.
    return toRichtext(shortDescription);
  }

  @Override
  public Markup getLongDescription() {
    String longDescription = getStringValue(getDelegate(), "longDescription");
    //long description by WCS is already html and encoded
    return toRichtext(longDescription, false);
  }

  @Override
  public String getTitle() {
    return getStringValue(getDelegate(), "title");
  }

  @Override
  public String getMetaDescription() {
    return getStringValue(getDelegate(), "metaDescription");
  }

  @Override
  public String getMetaKeywords() {
    return getStringValue(getDelegate(), "metaKeyword");
  }

  @Nullable
  @Override
  public BigDecimal getListPrice() {
    WcPrices priceInfo = getPriceInfo();
    if (priceInfo == null) {
      return null;
    }

    Map<String, WcPrice> prices = priceInfo.getPrices();
    if (prices == null || prices.isEmpty()) {
      return null;
    }

    WcPrice listPrice = prices.get("Display");
    if (listPrice == null) {
      return null;
    }

    return convertStringToBigDecimal(listPrice.getPriceValue());
  }

  @Nullable
  @Override
  public BigDecimal getOfferPrice() {
    UserContext userContext = CurrentUserContext.get();

    if (UserContextHelper.getForUserName(userContext) != null && StoreContextHelper.isDynamicPricingEnabled(getContext())) {
      return getPersonalizedOfferPrice();
    }

    WcPrices priceInfo = getPriceInfo();
    if (priceInfo == null) {
      return null;
    }

    Map<String, WcPrice> prices = priceInfo.getPrices();
    if (prices == null || prices.isEmpty()) {
      return null;
    }

    WcPrice offerPrice = prices.get("Offer");
    if (offerPrice != null && !offerPrice.getPriceValue().isEmpty()) {
      return convertStringToBigDecimal(offerPrice.getPriceValue());
    } else {
      return getPersonalizedOfferPrice();
    }
  }

  @Nullable
  private BigDecimal getPersonalizedOfferPrice() {
    UserContext userContext = CurrentUserContext.get();

    PersonalizedPriceByExternalIdCacheKey cacheKey = new PersonalizedPriceByExternalIdCacheKey(
            getExternalId(), getContext(), userContext, getCatalogWrapperService(), getCommerceCache());

    return getCommerceCache().find(cacheKey)
            .map(WcPrice::getPriceValue)
            .map(this::convertStringToBigDecimal)
            .orElse(null);
  }

  @Nullable
  @Override
  public String getSeoSegment() {
    return SeoSegmentHelper.getSeoSegment(getDelegate(), getContext());
  }

  @Nullable
  @Override
  public String getDefaultImageAlt() {
    return getStringValue(getDelegate(), "fullImageAltDescription");
  }

  @Nullable
  @Override
  public String getDefaultImageUrl() {
    return DataMapHelper.findString(getDelegate(), "fullImage")
            .flatMap(fullImage -> getLinkService().getImageUrl(fullImage, getContext()))
            .orElse(null);
  }

  @Nullable
  @Override
  public String getThumbnailUrl() {
    return DataMapHelper.findString(getDelegate(), "thumbnail")
            .flatMap(thumbnail -> getLinkService().getImageUrl(thumbnail, getContext()))
            .orElse(null);
  }

  /**
   * Returns a "ready-to-use" storefront url that points to the shop product page.
   * Normally (when the commerce adapter supports it) it would come from the commerce adapter/system.
   * Until that happens we simulate this by computing this value for ourselves.
   * Todo: change this implementation to read the url from delegate or use a template or whatsoever
   */
  @Override
  public String getStorefrontUrl() {
    return wcsUrlProvider.buildProductLink(this, emptyList(), false)
            .map(UriComponentsBuilder::toUriString)
            .orElse(EMPTY_URL);
  }

  @NonNull
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
            .map(assetService -> assetService.findVisuals(getReference(), false))
            .orElseGet(Collections::emptyList);
  }

  @NonNull
  @Override
  public List<Content> getDownloads() {
    return findAssetService()
            .map(assetService -> assetService.findDownloads(getReference()))
            .orElseGet(Collections::emptyList);
  }

  @Nullable
  @Override
  public Category getCategory() {
    return doGetCategory();
  }

  @NonNull
  @Override
  public List<Category> getCategories() {
    Category category = doGetCategory();
    return Collections.singletonList(category);
  }

  @Override
  public Currency getCurrency() {
    return StoreContextHelper.getCurrency(getContext());
  }

  @Override
  public boolean isVariant() {
    return DataMapHelper.findString(getDelegate(), "parentCatalogEntryID")
            .isPresent();
  }

  @NonNull
  @Override
  public List<ProductAttribute> getDefiningAttributes() {
    if (definingAttributes == null) {
      loadAttributes();
    }
    return definingAttributes;
  }

  @NonNull
  @Override
  public List<ProductAttribute> getDescribingAttributes() {
    if (describingAttributes == null) {
      loadAttributes();
    }
    return describingAttributes;
  }

  @Override
  @NonNull
  public List<Object> getAttributeValues(@NonNull String attributeId) {
    List<Object> values = new ArrayList<>();

    List<ProductAttribute> describingAttributes = getDescribingAttributes();
    for (ProductAttribute attribute : describingAttributes) {
      if (attributeId.equals(attribute.getId())) {
        values.addAll(attribute.getValues());
        break;
      }
    }

    if (!values.isEmpty()) {
      return values;
    }

    List<ProductAttribute> definingAttributes = getDefiningAttributes();
    for (ProductAttribute attribute : definingAttributes) {
      if (attributeId.equals(attribute.getId())) {
        values.addAll(attribute.getValues());
        if (values.isEmpty()) {
          VariantFilter variantFilter = null;
          values.addAll(getVariantAxisValues(attribute.getId(), variantFilter));
        }
        break;
      }
    }

    return values;
  }

  protected void loadAttributes() {
    // load attributes in local member variables if available in delegate
    definingAttributes = new ArrayList<>();
    describingAttributes = new ArrayList<>();

    List<Map<String, Object>> wcAttributes = DataMapHelper.getList(getDelegate(), "attributes");
    for (Map<String, Object> wcAttribute : wcAttributes) {
      ProductAttribute pa = new ProductAttributeImpl(wcAttribute);
      if (USAGE_DEFINING.equals(getStringValue(wcAttribute, "usage"))) {
        definingAttributes.add(pa);
      } else {
        describingAttributes.add(pa);
      }
    }
  }

  @Nullable
  protected BigDecimal convertStringToBigDecimal(String value) {
    if (!NumberUtils.isNumber(value)) {
      return null;
    }

    return NumberUtils.createBigDecimal(value);
  }

  // --- internal ---------------------------------------------------

  @NonNull
  private Category doGetCategory() {
    Catalog catalog = getCatalog(this)
            .orElseThrow(() -> new IllegalStateException("Product '" + this + "' does not have a catalog."));

    List<String> parentCategoryIds = DataMapTransformationHelper.getParentCatGroupIdForSingleWrapper(getDelegate(),
            catalog.getExternalId());

    return parentCategoryIds.stream()
            .filter(categoryId -> !isNullOrEmpty(categoryId) && !"-1".equals(categoryId))
            .map(this::findCategoryOrLog)
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Product '" + this + "' does not have a parent category."));
  }

  @Nullable
  private Category findCategoryOrLog(@NonNull String categoryId) {
    CommerceId commerceId = getCommerceIdProvider().formatCategoryTechId(getCatalogAlias(), categoryId);
    Category category = getCatalogService().findCategoryById(commerceId, getContext());
    if (category == null) {
      LOG.debug("Product '{}' points to an invalid category: {}", this.getId(), commerceId);
      return null;
    }
    return category;
  }

  @Nullable
  private static String getStringValue(@NonNull Map<String, Object> map, @NonNull String key) {
    return DataMapHelper.findString(map, key).orElse(null);
  }
}
