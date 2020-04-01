package com.coremedia.blueprint.assets.cae;

import com.coremedia.blueprint.assets.contentbeans.AMTaxonomy;
import com.coremedia.blueprint.cae.search.Condition;
import com.coremedia.blueprint.cae.search.SearchConstants;
import com.coremedia.blueprint.cae.search.SearchQueryBean;
import com.coremedia.blueprint.cae.search.SearchResultBean;
import com.coremedia.blueprint.cae.search.SearchResultFactory;
import com.coremedia.blueprint.cae.search.Value;
import com.coremedia.blueprint.cae.search.ValueAndCount;
import com.coremedia.blueprint.cae.search.facet.FacetResult;
import com.coremedia.blueprint.cae.search.facet.FacetValue;
import com.coremedia.blueprint.cae.search.solr.SolrQueryBuilder;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DownloadPortalSearchService implements InitializingBean {

  private static final Logger LOG = LoggerFactory.getLogger(DownloadPortalSearchService.class);

  static final String ASSETHIERARCHY_SOLR_FIELD = "assethierarchy";
  static final String ASSETTAXONOMY_SOLR_FIELD = "assettaxonomy";
  private static final String TITLE_SORT_DIRECTION = " ASC";

  private static final String CATEGORY_PATH_SEPARATOR = "/";
  private static final String CATEGORY_ROOT_PREFIX = "0/";

  private long cacheTimeInSecs = 60;

  private ContentBeanFactory contentBeanFactory;

  private ContentRepository contentRepository;

  private boolean isPreview = false;

  private SearchResultFactory searchResultFactory;

  private DataViewFactory dataViewFactory;

  // package private access for testing purposes
  List<String> assetTypes = Collections.emptyList();

  @Override
  public void afterPropertiesSet() throws Exception {
    // retrieve the list of concrete asset types
    assetTypes = AMUtils.getAssetSubtypes(contentRepository);
  }

  @Required
  public void setSearchResultFactory(SearchResultFactory searchResultFactory) {
    this.searchResultFactory = searchResultFactory;
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
  public void setDataViewFactory(DataViewFactory dataViewFactory) {
    this.dataViewFactory = dataViewFactory;
  }

  /**
   * Pass <code>true</code> to disable query caching for preview environments. Default is <code>false</code>.
   *
   * @param preview <code>true</code> to disable caching.
   */
  public void setPreview(boolean preview) {
    this.isPreview = preview;
  }

  /**
   * Sets the cache time of Solr query
   *
   * @param cacheTimeInSecs the cache time in seconds
   */
  public void setCacheTimeInSecs(long cacheTimeInSecs) {
    this.cacheTimeInSecs = cacheTimeInSecs;
  }

  public List<Subcategory> getSubCategories(@Nullable AMTaxonomy category) {
    SearchQueryBean assetQueryBean = createAssetQueryBean(null);
    // only querying subcategories so there is no need to return assets
    assetQueryBean.setLimit(0);

    String prefixedTaxonomyPath = getPrefixedCategoryPath(category);
    assetQueryBean.setFacetFields(Collections.singletonList(ASSETHIERARCHY_SOLR_FIELD));
    assetQueryBean.setFacetPrefix(prefixedTaxonomyPath);
    assetQueryBean.setFacetMinCount(1);

    SearchResultBean searchResult = querySearchEngine(assetQueryBean);

    // create the list of subcategories from the results of the facet query
    FacetResult facetResult = searchResult.getFacetResult();
    Collection<FacetValue> facets = facetResult.getFacets().getOrDefault(ASSETHIERARCHY_SOLR_FIELD, Collections.emptyList());
    return createSubcategoriesFromFacets(facets, prefixedTaxonomyPath);
  }

  public SearchResultBean getAssetsForCategory(@NonNull AMTaxonomy category, int assetsPerPage, int page) {
    Condition taxFilter = Condition.is(ASSETTAXONOMY_SOLR_FIELD, Value.exactly(String.valueOf(category.getContentId())));
    return createSearchResult(page, assetsPerPage, taxFilter, null);
  }

  public SearchResultBean getAssetsForSubject(@NonNull CMTaxonomy subject, int assetsPerPage, int page) {
    Condition taxFilter = Condition.is(SearchConstants.FIELDS.SUBJECT_TAXONOMY, Value.exactly(String.valueOf(subject.getContentId())));
    return createSearchResult(page, assetsPerPage, taxFilter, null);
  }

  /**
   * <p>
   * Creates the {@link PaginatedAssets} model based on the given parameters.
   * </p>
   *
   * @param query    the query to use
   * @param assetsPerPage the number of assets per page
   * @param pageNo     the requested page number
   * @return the {@link PaginatedAssets} that represents the requested category assets.
   */
  @NonNull
  public SearchResultBean searchForAssets(@NonNull String query,
                                                     int assetsPerPage,
                                                     int pageNo) {
    return createSearchResult(pageNo, assetsPerPage, null, query);
  }


  /*************************    private methods    *************************/

  @NonNull
  private List<Subcategory> createSubcategoriesFromFacets(@NonNull Collection<? extends ValueAndCount> facets,
                                                          @NonNull String pathPrefix) {
    List<Subcategory> subcategories = new ArrayList<>();
    for (ValueAndCount facet : facets) {
      String pathFromFacet = facet.getValue();

      // double checking if the #setFacetPrefix did actually work - it is basically always true
      if (pathFromFacet.startsWith(pathPrefix)) {
        AMTaxonomy subcategory = null;
        String subcategoryId = pathFromFacet.substring(pathPrefix.length());

        try {
          Content taxonomyContent = contentRepository.getContent(subcategoryId);
          subcategory = contentBeanFactory.createBeanFor(taxonomyContent, AMTaxonomy.class);
          subcategory = dataViewFactory.loadCached(subcategory, null);
        } catch (RuntimeException e) {
          LOG.error("Could not create asset category subcategory with id {} because of:", subcategoryId, e.getMessage());
          LOG.debug("Exception thrown", e);
        }

        if (subcategory != null) {
          subcategories.add(new Subcategory(subcategory, facet.getCount()));
        } else {
          LOG.warn("AMTaxonomy with id {} does not exist. " +
                  "Probably the content was just deleted but not removed from the search index yet.", subcategoryId);
        }
      }
    }
    // Sort subcategories by their names alphabetically - collator provides advanced ordering in terms of natural language
    Collections.sort(subcategories, new Comparator<Subcategory>() {
      @Override
      public int compare(Subcategory o1, Subcategory o2) {
        String subcategoryTitle1 = StringUtils.defaultString(o1.getCategory().getValue());
        String subcategoryTitle2 = StringUtils.defaultString(o2.getCategory().getValue());
        return Collator.getInstance().compare(subcategoryTitle1, subcategoryTitle2);
      }
    });
    return subcategories;
  }

  @NonNull
  private SearchResultBean querySearchEngine(SearchQueryBean queryBean) {
    SearchResultBean searchResult;
    if (isPreview) {
      searchResult = searchResultFactory.createSearchResultUncached(queryBean);
    } else {
      searchResult = searchResultFactory.createSearchResult(queryBean, cacheTimeInSecs);
    }
    return searchResult;
  }

  @NonNull
  private SearchQueryBean createAssetQueryBean(@Nullable String fulltextQuery) {
    SearchQueryBean assetQueryBean = createSearchQueryBean();

    if (StringUtils.isEmpty(fulltextQuery)) {
      // this is the default handler
      assetQueryBean.setSearchHandler(SearchQueryBean.SEARCH_HANDLER.DYNAMICCONTENT);
      assetQueryBean.setQuery(SolrQueryBuilder.ANY_FIELD_ANY_VALUE);
    } else {
      assetQueryBean.setSearchHandler(SearchQueryBean.SEARCH_HANDLER.FULLTEXT);
      assetQueryBean.setQuery(fulltextQuery);
    }
    assetQueryBean.addFilter(
            Condition.is(SearchConstants.FIELDS.DOCUMENTTYPE, Value.anyOf(assetTypes))
    );
    // this filter is actually only needed if this is not a search assets of a category, but does not harm much
    assetQueryBean.addFilter(
            Condition.is(ASSETTAXONOMY_SOLR_FIELD, Value.exactly(SolrQueryBuilder.FIELD_SET_ANY_VALUE))
    );
    return assetQueryBean;
  }

  SearchQueryBean createSearchQueryBean() {
    return new SearchQueryBean();
  }

  @NonNull
  static String getPrefixedCategoryPath(@Nullable AMTaxonomy category) {
    if (category == null) {
      return CATEGORY_ROOT_PREFIX;
    }

    List<? extends CMTaxonomy> categoryPathList = category.getTaxonomyPathList();
    StringBuilder prefixedCategoryPath = new StringBuilder();

    // prepend hierarchy depth prefix
    prefixedCategoryPath.append(categoryPathList.size());

    // append category hierarchy with content id as segments
    for (CMTaxonomy categoryPathSegment : categoryPathList) {
      prefixedCategoryPath.append(CATEGORY_PATH_SEPARATOR).append(categoryPathSegment.getContentId());
    }
    prefixedCategoryPath.append(CATEGORY_PATH_SEPARATOR);
    return prefixedCategoryPath.toString();
  }

  @NonNull
  private SearchResultBean createSearchResult(int pageNo,
                                              int hitsPerPage,
                                              @Nullable Condition taxFilter,
                                              @Nullable String query) {

    SearchQueryBean assetQueryBean = createAssetQueryBean(query);
    if (null != taxFilter) {
      assetQueryBean.addFilter(taxFilter);
    }

    assetQueryBean.setOffset(calculateDocumentOffset(pageNo, hitsPerPage));
    assetQueryBean.setLimit(hitsPerPage);

    assetQueryBean.setSortFields(Collections.singletonList(SearchConstants.FIELDS.TITLE.toString() + TITLE_SORT_DIRECTION));

    SearchResultBean searchResultBean = querySearchEngine(assetQueryBean);
    // a request to page > 1 would always return a result - this seems to be a request of a bookmark or it was manipulated
    // fallback to page 1 in these cases
    if (pageNo > 1 && searchResultBean.getHits().isEmpty()) {
      return createSearchResult(1, hitsPerPage, taxFilter, query);
    }

    return searchResultBean;
  }

  /**
   * Calculate the document offset for the search query based on the current page number.
   */
  private static int calculateDocumentOffset(int pageNo, int hitsPerPage) {
    return pageNo <= 0 ? 0 : (pageNo - 1) * hitsPerPage;
  }
}
