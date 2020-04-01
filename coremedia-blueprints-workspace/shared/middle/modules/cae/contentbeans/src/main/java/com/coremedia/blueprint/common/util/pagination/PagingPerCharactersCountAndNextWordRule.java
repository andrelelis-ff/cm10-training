package com.coremedia.blueprint.common.util.pagination;

class PagingPerCharactersCountAndNextWordRule implements PagingRule {
  private static final int DEFAULT_MAX_CHARACTERS_PER_PAGE = 2000;
  private Paginator paginator;
  private int maxCharactersPerPage = DEFAULT_MAX_CHARACTERS_PER_PAGE;

  @Override
  public void setPaginator(Paginator paginator) {
    this.paginator = paginator;
  }

  @Override
  public void setPagingUnitsNumber(int pagingUnitsNumber) {
    maxCharactersPerPage = pagingUnitsNumber;
  }

  @Override
  public boolean match(String localName) {
    if (paginator==null) {
      throw new IllegalStateException("Must set a paginator before using match");
    }
    return (paginator.getCharacterCounter() > maxCharactersPerPage);
  }

  @Override
  public int getPagingUnitsNumber() {
    return maxCharactersPerPage;
  }

}