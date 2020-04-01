package com.coremedia.blueprint.cae.action.search;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.search.Condition;
import com.coremedia.blueprint.cae.search.SearchConstants;
import com.coremedia.blueprint.cae.search.SearchQueryBean;
import com.coremedia.blueprint.cae.search.SearchResultBean;
import com.coremedia.blueprint.cae.search.SearchResultFactory;
import com.coremedia.blueprint.cae.search.Value;
import com.coremedia.blueprint.cae.search.ValueAndCount;
import com.coremedia.blueprint.cae.search.facet.FacetValue;
import com.coremedia.blueprint.cae.search.solr.SolrSearchParams;
import com.coremedia.blueprint.cae.searchsuggestion.Suggestion;
import com.coremedia.blueprint.cae.searchsuggestion.Suggestions;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.CollectionUtils;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.cae.search.SearchConstants.FIELDS.LOCATION_TAXONOMY;
import static com.coremedia.blueprint.cae.search.SearchConstants.FIELDS.SUBJECT_TAXONOMY;

/**
 * The Search service used for fulltext and autocomplete search
 */
public class SearchService {

  private static final Logger LOG = LoggerFactory.getLogger(SearchService.class);

  private static final int HITS_PER_PAGE_DEFAULT = 10;

  /** The content setting that configures the map from names of requested search facets to index fields */
  public static final String SETTING_FACETS = "searchFacets";
  /** The content setting that configures the number of values to request for a facet */
  public  static final String SETTING_FACET_LIMIT = "searchFacetLimit";

  private SearchResultFactory resultFactory;
  private boolean highlightingEnabled = false;

  private ContentBeanFactory contentBeanFactory;
  private ContentRepository contentRepository;
  private SettingsService settingsService;

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @Required
  public void setContentBeanFactory(ContentBeanFactory contentBeanFactory) {
    this.contentBeanFactory = contentBeanFactory;
  }

  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  @Required
  public void setResultFactory(SearchResultFactory resultFactory) {
    this.resultFactory = resultFactory;
  }

  public void setHighlightingEnabled(boolean highlightingEnabled) {
    this.highlightingEnabled = highlightingEnabled;
  }

  /**
   * the fulltext search method
   *
   * @param page       the search result page
   * @param searchForm SearchFormBean
   * @param docTypes the doctypes to use for the search
   * @return the search result
   */
  public SearchResultBean search(Page page, SearchFormBean searchForm, Collection<String> docTypes) {
    if (StringUtils.isEmpty(searchForm.getQuery())) {
      return null;
    }
    CMContext context = page.getContext();

    // get max hits settings
    int hitsPerPage = settingsService.settingWithDefault("searchResultHitsPerPage", Integer.class, 0, context);

    // fallback to old setting or HITS_PER_PAGE_DEFAULT
    if (hitsPerPage == 0) {
      hitsPerPage = settingsService.settingWithDefault("search.result.hitsPerPage", Integer.class, HITS_PER_PAGE_DEFAULT, context);
    }

    // build query
    SearchQueryBean searchQuery = new SearchQueryBean();
    searchQuery.setSearchHandler(SearchQueryBean.SEARCH_HANDLER.FULLTEXT);
    // add query string
    if (StringUtils.isNotEmpty(searchForm.getQuery())) {
      searchQuery.setQuery(searchForm.getQuery());
    }
    searchQuery.setSpellcheckSuggest(true);
    if (highlightingEnabled) {
      searchQuery.setHighlightingEnabled(true);
    }
    int rootChannelId = page.getNavigation().getRootNavigation().getContentId();
    // set channel filter from form
    if (StringUtils.isNotEmpty(searchForm.getChannelId()) && !searchForm.getChannelId().equals("" + rootChannelId)) {
      Content content = contentRepository.getContent(IdHelper.formatContentId(searchForm.getChannelId()));
      CMNavigation channel = contentBeanFactory.createBeanFor(content, CMNavigation.class);
      StringBuilder builder = new StringBuilder();
      for (Linkable aChannel : channel.getNavigationPathList()) {
        if(aChannel instanceof CMNavigation) {
          builder.append("\\/").append(((CMNavigation)aChannel).getContentId());
        }
      }

      Condition channelCondition = Condition.is(SearchConstants.FIELDS.NAVIGATION_PATHS, Value.exactly(builder.toString()));
      searchQuery.addFilter(channelCondition);
    } else {
      // set root channel id filter to limit results to the site the given action's default context belongs to
      searchQuery.addFilter(Condition.is(SearchConstants.FIELDS.NAVIGATION_PATHS, Value.exactly("\\/" + rootChannelId)));
    }
    // set doctypes to filter if applied in form
    if (StringUtils.isNotEmpty(searchForm.getDocTypeEscaped())) {
      Condition docTypeCondition = Condition.is(SearchConstants.FIELDS.DOCUMENTTYPE,
              Value.exactly(searchForm.getDocTypeEscaped()));
      searchQuery.addFilter(docTypeCondition);
    }
    //else apply doctypes that configured for filtering in the settings
    else if(docTypes != null && !docTypes.isEmpty()) {
      Condition docTypeCondition = Condition.is(SearchConstants.FIELDS.DOCUMENTTYPE,
              Value.anyOf(docTypes));
      searchQuery.addFilter(docTypeCondition);
    }

    // add facets
    addFacetQuery(searchQuery, context);

    // add filter queries for selected facet values
    searchQuery.setFacetFilters(searchForm.getFacetFilters());

    // add limit/offset
    searchQuery.setLimit(hitsPerPage);
    searchQuery.setOffset(searchForm.getPageNum() * hitsPerPage);
    // set sort criteria
    if (searchForm.isSortByDate()) {
      searchQuery.setSortFields(Collections.singletonList(SearchConstants.FIELDS.EXTERNALLY_DISPLAYED_DATE.toString()));
    }
    // run query
    return resultFactory.createSearchResultUncached(searchQuery);
  }

  /**
   * Adapts the given {@link SearchQueryBean} to request faceting results if content settings for the given
   * context configure faceting with settings key {@link #SETTING_FACETS}.
   *
   * @param searchQuery {@link SearchQueryBean}
   * @param context context to look up settings
   * @since 1810
   */
  private void addFacetQuery(SearchQueryBean searchQuery, CMContext context) {
    Map<String, String> facets = settingsService.settingAsMap(SETTING_FACETS, String.class, String.class, context);
    searchQuery.setFacetFieldsMap(facets);

    settingsService.getSetting(SETTING_FACET_LIMIT, Integer.class, context)
      .ifPresent(searchQuery::setFacetLimit);

    searchQuery.setFacetMinCount(SolrSearchParams.FACET_MIN_COUNT);
  }

  /**
   * The search query executed to find topic pages for the given search term
   * @param searchForm The user inputted search data.
   * @param taxonomyDocumentTypes taxonomy document type names, empty or null for no topic page search
   * @param topicIndexFields list of index fields with IDs of taxonomy documents, if empty or null defaults to
   *        {@link SearchConstants.FIELDS#SUBJECT_TAXONOMY} and {@link SearchConstants.FIELDS#LOCATION_TAXONOMY}
   * @return Topic search results
   */
  @Nullable
  public SearchResultBean searchTopics(Navigation navigation, SearchFormBean searchForm,
                                       @Nullable Collection<String> taxonomyDocumentTypes,
                                       @Nullable Collection<String> topicIndexFields) {
    // 1) search for all taxonomy documents matching the query from the search form
    //    and create a map from numeric content ID to these candidate taxonomy content beans
    List<?> hits = searchTaxonomies(searchForm, taxonomyDocumentTypes);
    ImmutableMap<String, CMLinkable> candidateTaxonomiesByNumericId = FluentIterable.from(hits)
            .filter(CMLinkable.class)
            .uniqueIndex(new Function<CMLinkable, String>() {
              @Override
              public String apply(@Nullable CMLinkable hit) {
                if (hit == null) {
                  throw new AssertionError();
                }
                return String.valueOf(hit.getContentId());
              }
            });
    if (candidateTaxonomiesByNumericId.isEmpty()) {
      return null;
    }

    // 2) Restrict the found taxonomy documents to the ones used in the current site,
    //    i.e. taxonomy documents where referring documents exist that are below the current root channel.
    //    To this end, search for such documents and get the actually used taxonomies via faceting.
    //    The default index fields with taxonomy IDs are for subject and location taxonomies, but may be set
    //    differently by the caller to match the used taxonomy document types
    List<String> indexFields = topicIndexFields == null || topicIndexFields.isEmpty()
            ? ImmutableList.of(SUBJECT_TAXONOMY.toString(), LOCATION_TAXONOMY.toString())
            : ImmutableList.copyOf(topicIndexFields);

    SearchQueryBean query = new SearchQueryBean();
    int rootChannelId = navigation.getRootNavigation().getContentId();
    query.addFilter(Condition.is(SearchConstants.FIELDS.NAVIGATION_PATHS, Value.exactly("\\/" + rootChannelId)));
    query.setFacetFields(indexFields);
    query.setFacetMinCount(1);
    query.setFacetLimit(1000);
    query.setLimit(0); // just interested in faceting results

    // restrict the query to candidate taxonomies, e.g "subjecttaxonomy:(42 OR 44) OR locationtaxonomy:(42 OR 44)"
    Joiner orJoiner = Joiner.on(" OR ");
    final String taxonomyIds = orJoiner.join(candidateTaxonomiesByNumericId.keySet());
    query.setQuery(FluentIterable.from(indexFields).transform(new Function<String, String>() {
      @Override
      public String apply(@Nullable String indexField) {
        return indexField + ":(" + taxonomyIds + ')';
      }
    }).join(orJoiner));

    // perform a faceted search and get the actually used taxonomies of the candidate taxonomies
    SearchResultBean searchResult = resultFactory.createSearchResultUncached(query);
    Map<String, Collection<FacetValue>> facets = searchResult.getFacetResult().getFacets();
    List<CMLinkable> resultTaxonomies = new ArrayList<>();
    for (String indexField : indexFields) {
      Collection<FacetValue> facetValues = facets.getOrDefault(indexField, Collections.emptyList());
      for (ValueAndCount valueAndCount : facetValues) {
        CMLinkable cmLinkable = candidateTaxonomiesByNumericId.get(valueAndCount.getValue());
        if (cmLinkable != null) {
          resultTaxonomies.add(cmLinkable);
        }
      }
    }

    // we have a result
    SearchResultBean result = new SearchResultBean();
    result.setHits(resultTaxonomies);
    result.setNumHits(resultTaxonomies.size());
    return result;
  }

  /**
   * Search for taxonomy documents matching the query from the search form.
   *
   * @param searchForm provides the user's query
   * @param taxonomyDocumentTypes taxonomy document type names, empty for empty result
   * @return taxonomy documents matching the query
   */
  private List<?> searchTaxonomies(SearchFormBean searchForm, @Nullable Collection<String> taxonomyDocumentTypes) {
    if (StringUtils.isEmpty(searchForm.getQuery()) || CollectionUtils.isEmpty(taxonomyDocumentTypes)) {
      return ImmutableList.of();
    }

    SearchQueryBean searchQuery = new SearchQueryBean();
    searchQuery.setSearchHandler(SearchQueryBean.SEARCH_HANDLER.DYNAMICCONTENT);
    searchQuery.addFilter(Condition.is(SearchConstants.FIELDS.DOCUMENTTYPE, Value.anyOf(taxonomyDocumentTypes)));

    // taxonomy value is fed into field teaserTitle
    searchQuery.setQuery(SearchConstants.FIELDS.TEASER_TITLE.toString() + ':' + searchForm.getQueryEscaped());
    searchQuery.setLimit(100);

    // run the query
    return resultFactory.createSearchResultUncached(searchQuery).getHits();
  }

  /**
   * the autocomplete search method
   *
   * @param rootNavigationId the page to retrieve the search ahead results for
   * @param term             the term the user is searching for
   * @param docTypes         the document types to restrict searching to
   * @return a list of suggestions
   */
  public Suggestions getAutocompleteSuggestions(String rootNavigationId, String term, Collection<String> docTypes) {
    Suggestions suggestions = new Suggestions();

    try {
      SearchQueryBean searchQuery = new SearchQueryBean();
      searchQuery.setSearchHandler(SearchQueryBean.SEARCH_HANDLER.SUGGEST);
      searchQuery.setQuery(term);
      // restrict to given site
      Condition cond = Condition.is(SearchConstants.FIELDS.NAVIGATION_PATHS, Value.exactly("\\/" + rootNavigationId));
      searchQuery.addFilter(cond);
      if (docTypes != null && !docTypes.isEmpty()) {
        searchQuery.addFilter(Condition.is(SearchConstants.FIELDS.DOCUMENTTYPE, Value.anyOf(docTypes)));
      }
      searchQuery.setLimit(1);

      LOG.debug("Getting suggestions for: {}", searchQuery.getQuery());

      SearchResultBean result = resultFactory.createSearchResultUncached(searchQuery);

      // add result to suggestions list
      List<ValueAndCount> suggestionValues = result.getAutocompleteSuggestions();

      List<Suggestion> suggestionList = new ArrayList<>();
      for (ValueAndCount vc : suggestionValues) {
        suggestionList.add(new Suggestion(vc.getValue(), vc.getValue(), vc.getCount()));
      }

      // sort suggestions by count and enforce limit
      Collections.sort(suggestionList);
      suggestions.addAll(suggestionList);

    } catch (Exception e) {
      LOG.error("Cannot retrieve suggestion", e);
    }

    return suggestions;
  }
}
