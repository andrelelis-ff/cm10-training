package com.coremedia.livecontext.ecommerce.ibm.search;

import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.search.SearchService;
import com.coremedia.livecontext.ecommerce.search.SuggestionResult;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * IBM Commerce Service implementation.
 */
public class SearchServiceImpl implements SearchService {
  private WcSearchWrapperService searchWrapperService;

  @Override
  public List<SuggestionResult> getAutocompleteSuggestions(String term, @NonNull StoreContext currentContext) {
    return searchWrapperService.getKeywordSuggestionsByTerm(term, currentContext).stream()
            .map(suggestion -> new SuggestionResult(suggestion.getTerm(), term, suggestion.getFrequency()))
            .collect(toList());
  }

  public WcSearchWrapperService getSearchWrapperService() {
    return searchWrapperService;
  }

  @Required
  public void setSearchWrapperService(WcSearchWrapperService searchWrapperService) {
    this.searchWrapperService = searchWrapperService;
  }
}
