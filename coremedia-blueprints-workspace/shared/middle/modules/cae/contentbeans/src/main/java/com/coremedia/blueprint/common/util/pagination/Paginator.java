package com.coremedia.blueprint.common.util.pagination;

import com.coremedia.xml.Markup;

import java.util.List;

public interface Paginator {

  int getBlockCounter();

  int getCharacterCounter();

  void setPagingRule(PagingRule pagingRule);

  PagingRule getPagingRule();

  List<Markup> split(Markup markup);
}


