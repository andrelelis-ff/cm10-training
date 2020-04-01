package com.coremedia.blueprint.common.util.pagination;

public interface PagingRule {

  int getPagingUnitsNumber();

  boolean match(String localName);

  void setPaginator(Paginator pagingFilter);

  void setPagingUnitsNumber(int pagingUnitsNumber);
}
