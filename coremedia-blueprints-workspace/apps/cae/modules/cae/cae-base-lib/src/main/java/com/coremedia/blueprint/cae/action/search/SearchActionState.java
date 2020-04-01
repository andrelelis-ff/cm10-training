package com.coremedia.blueprint.cae.action.search;

import com.coremedia.blueprint.cae.action.CMActionState;
import com.coremedia.blueprint.cae.search.SearchResultBean;
import com.coremedia.blueprint.common.contentbeans.CMAction;
import edu.umd.cs.findbugs.annotations.Nullable;


/**
 * The current state of the search action. Holds the action form as well as the action result
 *
 * @cm.template.api
 */
public class SearchActionState implements CMActionState {

  static final String ERROR_QUERY_TOO_SHORT = "queryTooShort";

  private final CMAction action;
  private final SearchFormBean form;
  private final SearchResultBean result;
  private final SearchResultBean topicsResult;
  private final int minimalSearchQueryLength;
  private String errorCode = null;

  public SearchActionState(CMAction action, SearchFormBean form, int minimalSearchQueryLength, SearchResultBean result, SearchResultBean topicsResult) {
    this.action = action;
    this.form = form;
    this.result = result;
    this.topicsResult = topicsResult;
    this.minimalSearchQueryLength = minimalSearchQueryLength;
  }

  public SearchActionState(CMAction action, SearchFormBean form, int minimalSearchQueryLength, String errorCode) {
    this(action, form, minimalSearchQueryLength, null, null);
    this.errorCode = errorCode;
  }

  public SearchActionState(CMAction action, int minimalSearchQueryLength) {
    this(action, new SearchFormBean(), minimalSearchQueryLength, null, null);
  }

  @Override
  public CMAction getAction() {
    return action;
  }

  /**
   * @cm.template.api
   */
  public SearchFormBean getForm() {
    return form;
  }

  /**
   * Returns the actual search result.
   *
   * @return search result or null if no search result is available, for example if {@link #isQueryTooShort()}
   * @cm.template.api
   */
  @Nullable
  public SearchResultBean getResult() {
    return result;
  }

  @Nullable
  public SearchResultBean getTopicsResult() {
    return topicsResult;
  }

  /**
   * @cm.template.api
   */
  public boolean isQueryTooShort() {
    return ERROR_QUERY_TOO_SHORT.equals(errorCode);
  }

  /**
   * @cm.template.api
   */
  public int getMinimalSearchQueryLength() {
    return minimalSearchQueryLength;
  }

  @Override
  public String toString() {
    return getClass().getName();
  }
}
