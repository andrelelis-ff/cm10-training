package com.coremedia.blueprint.cae.search.solr;

public interface SearchPreprocessor<SearchQueryBean> {
  void preProcess(SearchQueryBean sqb);
}
