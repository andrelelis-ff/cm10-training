package com.coremedia.blueprint.cae.search.solr;

import org.apache.solr.common.params.CommonParams;

public interface SolrSearchParams extends CommonParams {

  // Default value for maximum number of search results
  int MAX_LIMIT = 1000;

  // Solr Search Parameters
  String OFFSET = "o";
  int FACET_MIN_COUNT = 1;

}
