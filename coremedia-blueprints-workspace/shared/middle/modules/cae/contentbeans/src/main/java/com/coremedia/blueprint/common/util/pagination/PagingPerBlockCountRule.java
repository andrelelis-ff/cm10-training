package com.coremedia.blueprint.common.util.pagination;

import org.springframework.util.Assert;

/**
 * Defines a rule how many blocks (pagingUnitsNumber) should end up on a single page.
 * Blocks are defined as one of the elements p table pre blockquote ul ol.
 */
class PagingPerBlockCountRule implements PagingRule {
  private static final int DEFAULT_MAX_BLOCKS_PER_PAGE = 5;
  private static final String PAGE_SEPERATOR_TAGS = "p table pre blockquote ul ol";
  private Paginator paginator;
  private int maxBlocksPerPage = DEFAULT_MAX_BLOCKS_PER_PAGE;

  @Override
  public void setPaginator(Paginator pagingFilter) {
    this.paginator = pagingFilter;
  }

  @Override
  public void setPagingUnitsNumber(int pagingUnitsNumber) {
    maxBlocksPerPage = pagingUnitsNumber;
  }

  @Override
  public int getPagingUnitsNumber() {
    return maxBlocksPerPage;
  }

  @Override
  public boolean match(String localName) {
    if (paginator==null) {
      throw new IllegalStateException("Must set a paginator before using match");
    }
    Assert.notNull(localName);
    return (PAGE_SEPERATOR_TAGS.contains(localName)) && (paginator.getBlockCounter() >= maxBlocksPerPage);
  }

}
