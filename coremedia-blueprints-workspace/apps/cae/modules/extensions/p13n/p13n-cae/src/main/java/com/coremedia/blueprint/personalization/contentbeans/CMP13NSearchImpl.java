package com.coremedia.blueprint.personalization.contentbeans;

import com.coremedia.blueprint.cae.search.Condition;
import com.coremedia.blueprint.cae.search.SearchConstants;
import com.coremedia.blueprint.cae.search.SearchQueryBean;
import com.coremedia.blueprint.cae.search.SearchResultBean;
import com.coremedia.blueprint.cae.search.Value;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.common.util.SearchQueryUtil;
import com.coremedia.blueprint.personalization.search.SolrContextualSearchChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The bean corresponding to the <code>CMP13NSearch</code> document type. It represents
 * a Solr query that may contain search functions accessing the active user's context and thus
 * contextualizing the search.
 */
public class CMP13NSearchImpl extends CMP13NSearchBase {

  /*
   * DEVELOPER NOTE
   * You are invited to change this class by adding additional methods here.
   * Add them to the interface {@link com.coremedia.blueprint.personalization.contentbeans.CMP13NSearch}
   * to make them public.
   */

  private static final Logger LOGGER = LoggerFactory.getLogger(CMP13NSearchImpl.class);

  private SolrContextualSearchChecker solrContextualSearchChecker;

  public void setSolrContextualSearchChecker(SolrContextualSearchChecker solrContextualSearchChecker) {
    this.solrContextualSearchChecker = solrContextualSearchChecker;
  }

  /**
   * Returns the results of the search.
   *
   * @return list of teasers
   */
  @Override
  public List<CMTeasable> getItemsUnfiltered() {
    final String query = getSearchQuery();
    String statusMessageJSON = getSearchStatusAsJSON();
    if(statusMessageJSON == null) {
      final SearchQueryBean searchQueryBean = new SearchQueryBean();
      searchQueryBean.setQuery(query);

      addDocTypeFilter(searchQueryBean);
      addNavFilter(searchQueryBean);

      searchQueryBean.setLimit(getMaxLength());
      searchQueryBean.setSortFields(Arrays.asList(SearchConstants.FIELDS.MODIFICATION_DATE.toString()));

      final SearchResultBean searchResultUncached = getResultFactory().createSearchResultUncached(searchQueryBean);
      if(searchResultUncached.getNumHits() > 0) {
        //noinspection unchecked
        return (List<CMTeasable>) searchResultUncached.getHits();
      }
    } else {
      LOGGER.info("Search functions in query '{}' contain errors: {}", query, statusMessageJSON);
    }
    return getDefaultContent();
  }

  private void addNavFilter(SearchQueryBean searchQueryBean) {
    final List<? extends CMNavigation> navigationList = getNavigationList();
    if(!navigationList.isEmpty()) {
      final List<String> navigationPathExpr = new ArrayList<>();
      for (final Navigation nav : navigationList) {
        navigationPathExpr.add(toPathOfIDs(nav.getNavigationPathList()));
      }
      searchQueryBean.addFilter(Condition.is(SearchConstants.FIELDS.NAVIGATION_PATHS, Value.anyOf(navigationPathExpr)));
    }
  }

  private void addDocTypeFilter(SearchQueryBean searchQueryBean) {
    SearchQueryUtil.addDocumentTypeFilter(searchQueryBean, getDocumentType(), getContent().getRepository());
  }

  /**
   * Retrieves the status produces by the previous search as a JS dictionary in JSON.
   *
   * @return the search status or <code>null</code>
   */
  @Override
  public String getSearchStatusAsJSON() {
    return solrContextualSearchChecker.checkSearchFunctions(getSearchQuery());
  }

  /**
   * Transforms the supplied list of <code>CMNavigation</code> instances to a path representation consisting of
   * the content id of each element separated by '\/', i.e. an escaped slash.
   * This corresponds to how the <code>navigationpaths</code> field in the solr index is filled. Slashes must be
   * escaped in the Solr query syntax since version 4.0.
   */
  private static String toPathOfIDs(final List<? extends Linkable> path) {
    assert path != null;
    final StringBuilder idPath = new StringBuilder();
    for (final Linkable nav : path) {
      if(nav instanceof CMNavigation) {
        idPath.append("\\/").append(((CMNavigation)nav).getContentId());
      }

    }
    return idPath.toString();
  }
}
