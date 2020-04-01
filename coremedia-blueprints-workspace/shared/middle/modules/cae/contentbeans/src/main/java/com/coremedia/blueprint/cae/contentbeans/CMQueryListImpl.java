package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.base.querylist.QueryListHelper;
import com.coremedia.blueprint.cae.search.Condition;
import com.coremedia.blueprint.cae.search.SearchConstants;
import com.coremedia.blueprint.cae.search.SearchQueryBean;
import com.coremedia.blueprint.cae.search.SearchResultBean;
import com.coremedia.blueprint.cae.search.Value;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMObject;
import com.coremedia.blueprint.common.layout.Pagination;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.util.ContentBeanSolrSearchFormatHelper;
import com.coremedia.blueprint.common.util.SettingsStructToSearchQueryConverter;
import com.coremedia.cache.Cache;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.coremedia.blueprint.base.querylist.QueryListHelper.filterDuplicatesAndSortFixedItems;
import static com.coremedia.blueprint.base.querylist.QueryListHelper.getFixedItemsInRange;
import static com.coremedia.blueprint.base.querylist.QueryListHelper.mergeItems;

/**
 * Generated extension class for beans of document type "CMQueryList".
 */
public class CMQueryListImpl extends CMQueryListBase {
  private static final Logger LOG = LoggerFactory.getLogger(CMQueryListImpl.class);

  private static final String ANNOTATED_LINK_STRUCT_INDEX_PROPERTY_NAME = "index";
  private static final String PAGINATION_SETTING = "loadMore";
  private static final String ITEMS_PER_PAGE_SETTING = "limit";


  private static final int CACHE_QUERY_FOR_IN_SECONDS = 5;

  private boolean preview;

  @org.springframework.beans.factory.annotation.Value("${cae.is.preview}")
  public void setPreview(boolean preview) {
    this.preview = preview;
  }


  // --- classic Container ------------------------------------------

  @Override
  public List<Linkable> getItems() {
    SearchQueryBean searchQuery = getSearchQuery(false);
    int limit = searchQuery.getLimit();
    List<Map<String, Object>> fixedItems = getFixedItemsInRange(getFixedItemsStructList(), 0, limit, ANNOTATED_LINK_STRUCT_INDEX_PROPERTY_NAME);
    int fixedItemsSize = fixedItems.size();

    if (limit > 0 && limit == fixedItemsSize) {
      // result only contains fixed items
      return fixedItems.stream()
          .map(CMQueryListImpl::fixedItemTarget)
          .flatMap(Optional::stream)
          .collect(Collectors.toList());
    } else {
      excludeSelfAndFixedItems(searchQuery, fixedItems);
      // result contains fixed and dynamic items
      return mergeFixedItems(fixedItems, searchResultToItems(createSearchResult(searchQuery)),limit);
    }
  }


  // --- Pagination -------------------------------------------------

  /**
   * Returns the value of the property "loadMore" in the local settings. Default is false.
   *
   * @return the value of the property "loadMore" in the local settings
   * @since 1901
   */
  @Override
  public boolean isPaginated() {
    return getSettingsService().settingWithDefault(PAGINATION_SETTING, Boolean.class, false, getContent());
  }

  @Override
  public Pagination asPagination(int pageNum) {
    List<Map<String, Object>> fixedItems = getFixedItemsStructList();
    PaginationImpl pagination = new PaginationImpl(this, pageNum, itemsPerPage(), fixedItems);

    SearchQueryBean searchQuery = getSearchQuery(true);
    excludeSelfAndFixedItems(searchQuery, fixedItems);
    searchQuery.setOffset(pagination.dynamicOffset());
    int limit = pagination.dynamicLimit();
    if (limit != -1) {
      searchQuery.setLimit(limit);
    }
    SearchResultBean searchResult = createSearchResult(searchQuery);
    List<Linkable> linkables = searchResultToItems(searchResult);
    pagination.setSearchResult(linkables, searchResult.getNumHits());
    return pagination;
  }


  // --- internal and more features ---------------------------------

  @VisibleForTesting
  int itemsPerPage() {
    int ipp = getSettingsService().settingWithDefault(ITEMS_PER_PAGE_SETTING, Integer.class, -1, this);
    return ipp>0 ? ipp : -1;
  }

  private void excludeSelfAndFixedItems(SearchQueryBean searchQuery, List<Map<String, Object>> fixedItems) {
    List<String> ids = new ArrayList<>();
    ids.add(ContentBeanSolrSearchFormatHelper.getContentBeanId(this));
    for (Map<String, Object> fixedItem : fixedItems) {
      Linkable target = fixedItemTarget(fixedItem).orElse(null);
      if (target instanceof CMObject) {
        ids.add(ContentBeanSolrSearchFormatHelper.getContentBeanId((CMObject) target));
      }
    }
    searchQuery.addFilter(Condition.isNot(SearchConstants.FIELDS.ID.toString(), Value.anyOf(ids)));
  }

  private SearchQueryBean getSearchQuery(boolean unlimited) {
    SettingsStructToSearchQueryConverter converter = new SettingsStructToSearchQueryConverter(
      this,
      getSitesService(),
      getSettingsService(),
      getContent().getRepository(),
      getContentBeanFactory(),
      unlimited);
    return converter.convert();
  }

  /**
   * Get the fixed items as struct list with valid indexes and without duplicate indexes and targets.
   *
   * @return the filtered fixed items
   */
  protected List<Map<String, Object>> getFixedItemsStructList() {
    // get valid fixed items from super class
    Map<String, List<Map<String, Object>>> fixedItemsMap = getAnnotatedLinkList(getExtendedItems(), getLegacyItems(), ITEMS);
    List<Map<String, Object>> fixedItems = fixedItemsMap.get(CMLinkableBase.ANNOTATED_LINKS_STRUCT_ROOT_PROPERTY_NAME);

    return filterDuplicatesAndSortFixedItems(fixedItems, ANNOTATED_LINK_STRUCT_INDEX_PROPERTY_NAME, ANNOTATED_LINK_STRUCT_TARGET_PROPERTY_NAME);
  }

  private static Optional<Linkable> fixedItemTarget(Map<String, Object> fixedItem) {
    return QueryListHelper.fixedItemTarget(fixedItem, ANNOTATED_LINK_STRUCT_TARGET_PROPERTY_NAME, Linkable.class);
  }

  private List<Linkable> searchResultToItems(SearchResultBean searchResult) {
    List<Linkable> searchItems = new ArrayList<>();

    // Map List<?> to List<Linkable>.
    // Just a technical cast, all hits are supposed to be of type Linkable.
    for (Object hit : searchResult.getHits()) {
      if (hit instanceof Linkable) {
        searchItems.add((Linkable) hit);
      } else {
        LOG.warn("Search result of {} contains an unexpected non-Linkable item {}. Features using limited queries or paginated views may show incomplete results.", this, hit);
      }
    }

    // Impl note: Currently the validation service has predicates for
    // a) validFrom/validTo.  This is already considered by the actual search,
    //    thus that predicate has no effect here.
    // b) Some model issues like teasers without target.  Such content should not exist
    //    in a real world live repository anyway, so these predicates cause no harm.
    // If the validation service is ever used for business logic which is not
    // equivalently applied by the search query, this post-filtering here would
    // falsify the result for limited searches, though.
    List<Linkable> filteredItems = filterItems(searchItems);
    if (filteredItems.size() != searchItems.size()) {
      LOG.warn("Post-filtered search result of {}. Features using limited queries or paginated views may show incomplete results.", this);
    }
    return filteredItems;
  }

  protected List<Linkable> mergeFixedItems(List<Map<String, Object>> fixedItems, List<Linkable> dynamicItems, int maxLength) {
    return mergeItems(fixedItems, dynamicItems, 0, maxLength, ANNOTATED_LINK_STRUCT_INDEX_PROPERTY_NAME, ANNOTATED_LINK_STRUCT_TARGET_PROPERTY_NAME, Linkable.class);
  }

  private SearchResultBean createSearchResult(SearchQueryBean searchQuery) {
    SearchResultBean searchResult;
    if (preview){
      searchResult = getResultFactory().createSearchResultUncached(searchQuery);
      Cache.uncacheable();
    } else {
      searchResult = getResultFactory().createSearchResult(searchQuery, CACHE_QUERY_FOR_IN_SECONDS);
    }
    return searchResult;
  }

  @Override
  @NonNull
  protected Map<String, Object> createAnnotatedLinkStructMap(@NonNull CMLinkable target, int index, @Nullable String linkListPropertyName) {
    Map<String, Object> targetStructMap = super.createAnnotatedLinkStructMap(target, index, linkListPropertyName);
    if (ITEMS.equals(linkListPropertyName)) {
      targetStructMap.put(ANNOTATED_LINK_STRUCT_INDEX_PROPERTY_NAME, index);
    }
    return targetStructMap;
  }

  @NonNull
  private Map<String, List<Map<String, Object>>> getExtendedItems() {
    return getAnnotatedLinkListUnfiltered(EXTENDED_ITEMS);
  }

  @NonNull
  private List<CMLinkable> getLegacyItems() {
    return getLegacyLinkListUnfiltered(ITEMS);
  }
}
