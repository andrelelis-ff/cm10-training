package com.coremedia.livecontext.asset;

import com.coremedia.blueprint.cae.search.Condition;
import com.coremedia.blueprint.cae.search.SearchConstants;
import com.coremedia.blueprint.cae.search.SearchQueryBean;
import com.coremedia.blueprint.cae.search.SearchResultBean;
import com.coremedia.blueprint.cae.search.SearchResultFactory;
import com.coremedia.blueprint.cae.search.Value;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.Site;
import com.coremedia.objectserver.beans.ContentBean;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CaeAssetSearchService implements AssetSearchService {

  private ContentRepository contentRepository;
  private SearchResultFactory searchResultFactory;
  private int resultLimit = 500;
  private long cacheForSeconds = 300;

  @NonNull
  @Override
  public List<Content> searchAssets(@NonNull String contentType, @NonNull String externalId, @NonNull Site site) {
    List<ContentBean> contentBeans = poseSolrQuery(contentType, site, externalId);
    List<Content> contents = new ArrayList<>(contentBeans.size());
    for (ContentBean contentBean : contentBeans) {
      contents.add(contentBean.getContent());
    }
    return contents;
  }

  private List<ContentBean> poseSolrQuery(@NonNull String contentType, Site site, String externalId) {
    SearchQueryBean query = createQueryBean();
    query.setSearchHandler(SearchQueryBean.SEARCH_HANDLER.DYNAMICCONTENT);
    query.setLimit(resultLimit);
    int rootChannelId = getSiteRootDocumentId(site);
    query.addFilter(Condition.is(SearchConstants.FIELDS.NAVIGATION_PATHS, Value.exactly("\\/" + rootChannelId)));
    query.setNotSearchableFlagIgnored(true);
    query.addFilter(Condition.is(SearchConstants.FIELDS.DOCUMENTTYPE, Value.anyOf(getSubTypesOf(contentType))));
    query.setQuery(SearchConstants.FIELDS.COMMERCE_ITEMS.toString() + ':' + '"' + externalId + '"');
    SearchResultBean searchResult = searchResultFactory.createSearchResult(query, cacheForSeconds);
    //noinspection unchecked
    return (List<ContentBean>) searchResult.getHits();
  }

  @VisibleForTesting
  SearchQueryBean createQueryBean() {
    return new SearchQueryBean();
  }

  @VisibleForTesting
  int getSiteRootDocumentId(Site site) {
    Content siteRootDocument = site.getSiteRootDocument();
    return IdHelper.parseContentId(siteRootDocument.getId());
  }

  @VisibleForTesting
  List<String> getSubTypesOf(String contentType) {
    ContentType type = contentRepository.getContentType(contentType);
    if (type == null) {
      throw new IllegalStateException("The configured content type '" + contentType + "' does not exist.");
    }

    Set<ContentType> subtypes = type.getSubtypes();
    List<String> escapedContentTypes = new ArrayList<>(subtypes.size());
    for (ContentType subtype : subtypes) {
      escapedContentTypes.add(escapeLiteralForSearch(subtype.getName()));
    }
    return escapedContentTypes;
  }

  @NonNull
  private String escapeLiteralForSearch(@NonNull String literal) {
    return '"' + CharMatcher.is('"').replaceFrom(literal, "\\\"") + '"';
  }

  @Required
  public void setSearchResultFactory(SearchResultFactory searchResultFactory) {
    this.searchResultFactory = searchResultFactory;
  }

  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  /**
   * Limits the result of the solr query. If this is set to a low value it can happen that some visuals are not shown on pdps.
   * Specially for pdps with spinners this is really relevant because a spinner has many pictures and each picture is part
   * of the result.
   */
  public void setResultLimit(int resultLimit) {
    this.resultLimit = resultLimit;
  }

  public void setCacheForSeconds(long cacheForSeconds) {
    this.cacheForSeconds = cacheForSeconds;
  }
}
