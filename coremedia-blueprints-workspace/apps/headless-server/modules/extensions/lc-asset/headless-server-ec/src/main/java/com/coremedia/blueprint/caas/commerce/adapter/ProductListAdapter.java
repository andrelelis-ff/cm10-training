package com.coremedia.blueprint.caas.commerce.adapter;

import com.coremedia.blueprint.base.caas.model.adapter.AbstractDynamicListAdapter;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.caas.commerce.model.CommerceFacade;
import com.coremedia.caas.model.adapter.ExtendedLinkListAdapterFactory;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.util.StructUtil;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.search.SearchResult;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@DefaultAnnotation(NonNull.class)
public class ProductListAdapter extends AbstractDynamicListAdapter<Object> {
  public static final String STRUCT_KEY_EXTERNAL_ID = "externalId";
  public static final String STRUCT_KEY_PRODUCTLIST = "productList";
  public static final String STRUCT_KEY_PRODUCTLIST_SELECT_FACET_VALUE = "selectedFacetValue";
  public static final String STRUCT_KEY_PRODUCTLIST_OFFSET = "offset";
  public static final String STRUCT_KEY_PRODUCTLIST_MAX_LENGTH = "maxLength";
  public static final String STRUCT_KEY_PRODUCTLIST_ORDER_BY = "orderBy";

  public static final int MAX_LENGTH_DEFAULT = 10;
  public static final int OFFSET_DEFAULT = 0;
  public static final String DIGIT_PATTERN = "[0-9]*";
  public static final String ALL_QUERY = "*";

  private final SettingsService settingsService;
  private final CommerceFacade commerceFacade;
  private final String siteId;

  public ProductListAdapter(ExtendedLinkListAdapterFactory extendedLinkListAdapterFactory, Content content, SettingsService settingsService, CommerceFacade commerceFacade, String siteId) {
    super(extendedLinkListAdapterFactory, content);
    this.settingsService = settingsService;
    this.commerceFacade = commerceFacade;
    this.siteId = siteId;
  }

  private int getOffset() {
    String value = StructUtil.getString(getSettings(), STRUCT_KEY_PRODUCTLIST_OFFSET);
    return value != null ? Integer.parseInt(value) : OFFSET_DEFAULT;
  }

  private int getMaxLength() {
    String value = StructUtil.getString(getSettings(), STRUCT_KEY_PRODUCTLIST_MAX_LENGTH);
    return value != null ? Integer.parseInt(value) : MAX_LENGTH_DEFAULT;
  }

  private String getOrderBy() {
    return StructUtil.getString(getSettings(), STRUCT_KEY_PRODUCTLIST_ORDER_BY);
  }

  private String getFacet() {
    String facetSettingValue = StructUtil.getString(getSettings(), STRUCT_KEY_PRODUCTLIST_SELECT_FACET_VALUE);
    String returnValue = "";
    if (facetSettingValue != null && !facetSettingValue.matches(DIGIT_PATTERN)) {
      returnValue = facetSettingValue;
    }
    return returnValue;
  }

  public List<Product> getProducts() {
    SearchResult<Product> productSearchResult = commerceFacade.searchProducts(ALL_QUERY, getSearchParams(), siteId);
    return productSearchResult != null ? productSearchResult.getSearchResult() : Collections.emptyList();
  }

  private Map<String, String> getSearchParams() {
    Category category = commerceFacade.getCategory(getExternalId(), siteId);
    Map<String, String> params = new HashMap<>();
    CatalogAlias catalogAlias = category != null ? category.getReference().getCatalogAlias() : null;
    String orderBy = getOrderBy();
    int limit = getMaxLength();
    int offset = getOffset();
    Optional<String> overrideCategoryId = getOverrideCategoryId();
    String facet = getFacet();

    //if necessary use the api which supports the facet search
    params.put(CatalogService.SEARCH_PARAM_FACET_SUPPORT, "true");

    if (overrideCategoryId.isPresent()) {
      params.put(CatalogService.SEARCH_PARAM_CATEGORYID, overrideCategoryId.get());
    } else if (category != null && !category.isRoot()) {
      params.put(CatalogService.SEARCH_PARAM_CATEGORYID, category.getExternalTechId());
    }

    if (catalogAlias != null && !StringUtils.isEmpty(catalogAlias.value())) {
      params.put(CatalogService.SEARCH_PARAM_CATALOG_ALIAS, catalogAlias.value());
    }

    if (!StringUtils.isEmpty(orderBy)) {
      params.put(CatalogService.SEARCH_PARAM_ORDERBY, orderBy);
    }

    if (limit >= 0) {
      params.put(CatalogService.SEARCH_PARAM_TOTAL, String.valueOf(limit));
    }

    if (offset > 0) {
      params.put(CatalogService.SEARCH_PARAM_OFFSET, String.valueOf(offset));
    }

    if (StringUtils.isNotEmpty(facet)) {
      params.put(CatalogService.SEARCH_PARAM_FACET, facet);
    }

    return params;
  }

  private Optional<String> getOverrideCategoryId() {
    String value = StructUtil.getString(getSettings(), STRUCT_KEY_PRODUCTLIST_SELECT_FACET_VALUE);
    if (value != null && value.matches(DIGIT_PATTERN)) {
      return Optional.of(value);
    }
    return Optional.empty();
  }

  private Struct getSettings() {
    return settingsService.setting(STRUCT_KEY_PRODUCTLIST, Struct.class, getContent());
  }

  private int getStart(@Nullable Integer offset) {
    return (offset != null && offset > 0) ? offset : 0;
  }

  private String getExternalId() {
    return getContent().getString(STRUCT_KEY_EXTERNAL_ID);
  }

  @Override
  public int getLimit() {
    return getMaxLength();
  }

  @Override
  public int getStart() {
    return getStart(getOffset());
  }

  @Override
  public List<Object> getDynamicItems() {
    // The cast to Object is necessary, because the dynamic products need to be the same class as or a superclass/interface
    // of Content.class (the fixed items are content items).
    return getProducts().stream().map(product -> (Object) product).collect(Collectors.toList());
  }

  @Override
  public Class<Object> getItemClass() {
    return Object.class;
  }
}
