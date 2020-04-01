package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.base.querylist.PaginationHelper;
import com.coremedia.blueprint.base.querylist.QueryListHelper;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.layout.Pagination;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class PaginationImpl implements Pagination {
  private static final Logger LOG = LoggerFactory.getLogger(PaginationImpl.class);

  private static final String ANNOTATED_LINK_STRUCT_INDEX_PROPERTY_NAME = "index";
  private static final String ANNOTATED_LINK_STRUCT_TARGET_PROPERTY_NAME = "target";

  private final CMLinkable linkTarget;
  private final int pageNum;

  private final int itemsPerPage;
  private final List<Map<String, Object>> fixedItems;

  private List<Linkable> pagedHits;
  private long numTotalHits;


  // --- Construct and configure ------------------------------------

  PaginationImpl(CMLinkable linkTarget,
                 int pageNum,
                 int itemsPerPage,
                 List<Map<String, Object>> fixedItems) {
    if (pageNum < 0) {
      throw new IllegalArgumentException("Negative pageNum: " + pageNum);
    }
    if (itemsPerPage<0 && pageNum>0) {
      throw new IllegalArgumentException("All items on one page and pageNum > 0 is contradictory.");
    }
    if (itemsPerPage == 0) {
      // Infinitely many pages, all empty
      LOG.warn("Pagination with 0 items per page does not really make sense for {}", linkTarget);
    }

    this.linkTarget = linkTarget;
    this.pageNum = pageNum;
    this.itemsPerPage = itemsPerPage<0 ? -1 : itemsPerPage;
    this.fixedItems = fixedItems;
  }

  void setSearchResult(List<Linkable> pagedHits, long numTotalHits) {
    this.pagedHits = pagedHits;
    this.numTotalHits = numTotalHits;
  }


  // --- Pagination -------------------------------------------------

  @Override
  public int getPageNum() {
    return pageNum;
  }

  @Override
  public long getNumberOfPages() {
    return numberOfPages(totalNumberOfItems());
  }

  @Override
  public int getItemsPerPage() {
    return itemsPerPage;
  }

  @Override
  public List<Linkable> getItems() {
    return paginate();
  }

  @Override
  public CMLinkable linkable() {
    return linkTarget;
  }


  // --- Package Features -------------------------------------------

  /**
   * Returns the search offset for this page
   * <p>
   * pageNum * itemsPerPage - &lt;number of fixed items before this page&gt;
   */
  int dynamicOffset() {
    int from = pageNum * itemsPerPage;
    return PaginationHelper.dynamicOffset(fixedItems, from, ANNOTATED_LINK_STRUCT_INDEX_PROPERTY_NAME);
  }

  /**
   * Returns the number of dynamic items needed for this page
   * <p>
   * itemsPerPage - &lt;number of fixed items on this page&gt;
   */
  int dynamicLimit() {
    if (itemsPerPage == -1) {
      // All items on one page -> no limit.
      return -1;
    }
    int from = pageNum * itemsPerPage;
    return PaginationHelper.dynamicLimit(fixedItems, from, itemsPerPage, ANNOTATED_LINK_STRUCT_INDEX_PROPERTY_NAME);
  }


  // --- Misc -------------------------------------------------------

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
            .add("delegate", linkTarget)
            .add("pageNum", pageNum)
            .add("itemsPerPage", itemsPerPage)
            .toString();
  }


  // --- internal ---------------------------------------------------

  private List<Linkable> paginatedItems(int from, int itemsPerPage) {
    List<Map<String, Object>> limitedFixedItems = QueryListHelper.getFixedItemsInRange(fixedItems, from, itemsPerPage, ANNOTATED_LINK_STRUCT_INDEX_PROPERTY_NAME);
    return QueryListHelper.mergeItems(limitedFixedItems, pagedHits, from, itemsPerPage, ANNOTATED_LINK_STRUCT_INDEX_PROPERTY_NAME, ANNOTATED_LINK_STRUCT_TARGET_PROPERTY_NAME, Linkable.class);
  }

  private List<Linkable> paginate() {
    if (itemsPerPage == 0) {
      // Infinitely many pages, all empty
      LOG.warn("Pagination with 0 items per page does not really make sense for {}", this);
      return Collections.emptyList();
    }

    int numDynamicItems = dynamicLimit();
    // The number of items (fixed + dynamic) of this page.
    // numDynamicItems and pagedHits size are normally the same, but may differ
    // * on the last page
    // * due to ValidationService post filtering
    int numItems = itemsPerPage - numDynamicItems + pagedHits.size();

    // For template convenience:
    // Always succeed for page #0, even if there are no items at all and
    // numberOfPages() says 0.  This allows templates to access page #0
    // hardcoded for the initial view of a paginated collection.
    // Strictly logically, this is wrong however.
    if (numItems==0 && pageNum==0) {
      return Collections.emptyList();
    }

    int fixedItemsfrom = pageNum * itemsPerPage;
    return paginatedItems(fixedItemsfrom, itemsPerPage);
  }

  @VisibleForTesting
  long totalNumberOfItems() {
    return fixedItems.size() + numTotalHits;
  }

  private long numberOfPages(long totalNumItems) {
    if (totalNumItems == 0) {
      return 0;
    }
    if (itemsPerPage == -1) {
      // no limit -> all items on one page
      return 1;
    }
    if (itemsPerPage == 0) {
      // "Infinitely" many pages, all empty
      return Integer.MAX_VALUE;
    }
    return totalNumItems / itemsPerPage + Long.signum(totalNumItems % itemsPerPage);
  }
}
