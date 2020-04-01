package com.coremedia.livecontext.fragment.resolver;

import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.blueprint.cae.search.Condition;
import com.coremedia.blueprint.cae.search.SearchConstants;
import com.coremedia.blueprint.cae.search.SearchQueryBean;
import com.coremedia.blueprint.cae.search.SearchResultBean;
import com.coremedia.blueprint.cae.search.SearchResultFactory;
import com.coremedia.blueprint.cae.search.Value;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.query.QueryService;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.fragment.FragmentParameters;
import com.coremedia.objectserver.beans.ContentBean;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * External Content resolver for 'externalRef' value that specifies a search term.
 *
 * <p>The implementation uses a search engine to find matching content for the search term. The search engine is
 * configured in {@link #setSearchResultFactory(com.coremedia.blueprint.cae.search.SearchResultFactory)}.
 */
public class SearchTermExternalReferenceResolver extends ExternalReferenceResolverBase implements InitializingBean {
  private static final Logger LOG = LoggerFactory.getLogger(SearchTermExternalReferenceResolver.class);

  public static final String PREFIX = "cm-searchterm:";
  public static final String CACHE_CLASS = SearchTermExternalReferenceResolver.class.getName();

  public static final String QUERY_NAVIGATION_WITH_SEGMENT = "TYPE " + CMNavigation.NAME + ": " + CMLinkable.SEGMENT + " = ?0";

  private Cache cache;
  private TreeRelation<Content> navigationTreeRelation;
  private SearchResultFactory searchResultFactory;

  private String segmentPath = "";
  private String contentType;
  private String field;
  private int cacheForSeconds = 60;

  private Iterable<String> segments;
  private Collection<String> escapedContentTypes;

  /**
   * Constructor initializes this external reference resolver with a prefix to enable the resolver
   * to match on a request.
   */
  public SearchTermExternalReferenceResolver() {
    super(PREFIX);
  }

  // --- configuration ----------------------------------------------

  /**
   * Sets the cache to use for caching external reference resolution.
   *
   * <p>Caching can be disabled by setting {@link #setCacheForSeconds(int)} to 0.
   *
   * <p>If caching is not disabled, a cache capacity for cache class {@link #CACHE_CLASS} needs to be configured at
   * the given cache..
   *
   * @param cache cache
   */
  @Required
  public void setCache(@NonNull Cache cache) {
    Objects.requireNonNull(cache);
    this.cache = cache;
  }

  /**
   * Sets the {@link com.coremedia.blueprint.base.tree.TreeRelation} to find the navigation with the configured
   * {@link #setSegmentPath(String)} in the navigation tree for a site.
   *
   * @param navigationTreeRelation navigation tree relation
   */
  @Required
  public void setNavigationTreeRelation(@NonNull TreeRelation<Content> navigationTreeRelation) {
    Objects.requireNonNull(navigationTreeRelation);
    this.navigationTreeRelation = navigationTreeRelation;
  }

  /**
   * Sets the {@link com.coremedia.blueprint.cae.search.SearchResultFactory}.
   *
   * @param searchResultFactory the {@link com.coremedia.blueprint.cae.search.SearchResultFactory}
   */
  @Required
  public void setSearchResultFactory(@NonNull SearchResultFactory searchResultFactory) {
    Objects.requireNonNull(searchResultFactory);
    this.searchResultFactory = searchResultFactory;
  }

  /**
   * Sets the segment path of the navigation context below which this resolver searches for matching content.
   *
   * <p>The path is relative to the {@link com.coremedia.cap.multisite.Site#getSiteRootDocument() site's root document}
   * and must not start with a slash. The first path segment identifies a direct child of the site's root document.
   * Further path segments are separated by slashes.
   *
   * <p>The navigation context identified by the given segment itself is not included in the search.
   *
   * <p>The default value is the empty string, which means that all contents below the site's root channel are
   * considered.
   *
   * @param segmentPath segment path relative to site root folder
   * @throws java.lang.IllegalArgumentException if the path starts with a slash
   */
  public void setSegmentPath(@NonNull String segmentPath) {
    Objects.requireNonNull(segmentPath);
    Preconditions.checkArgument(!segmentPath.startsWith("/"),
            "Segment path must be relative and not start with a slash: " + segmentPath);
    this.segmentPath = segmentPath;
    this.segments = Splitter.on('/').omitEmptyStrings().split(segmentPath);
  }

  /**
   * Sets the type of the content to resolve.
   *
   * <p>Instances of subtypes are found as well.
   *
   * @param contentType content type name
   */
  @Required
  public void setContentType(@NonNull String contentType) {
    Objects.requireNonNull(contentType);
    this.contentType = contentType;
  }

  /**
   * Sets the name of the string field to search in for the term specified in the external reference.
   *
   * <p>This field needs to exist in the search engine.
   *
   * @param field field to search in
   */
  @Required
  public void setField(@NonNull String field) {
    Objects.requireNonNull(field);
    this.field = field;
  }

  /**
   * Sets the time in seconds to cache the result.
   *
   * <p>The default is 60 seconds.
   *
   * @param cacheForSeconds time to cache the result or 0 for no caching
   */
  public void setCacheForSeconds(int cacheForSeconds) {
    this.cacheForSeconds = cacheForSeconds;
  }

  @Override
  public void afterPropertiesSet() {
    ContentType type = contentRepository.getContentType(contentType);
    if (type == null) {
      throw new IllegalStateException("The configured content type '" + contentType + "' does not exist.");
    }

    Set<ContentType> subtypes = type.getSubtypes();
    escapedContentTypes = new ArrayList<>(subtypes.size());
    for (ContentType subtype : subtypes) {
      escapedContentTypes.add(escapeLiteralForSearch(subtype.getName()));
    }
  }

  // --- interface --------------------------------------------------

  @Nullable
  @Override
  protected LinkableAndNavigation resolveExternalRef(@NonNull FragmentParameters fragmentParameters,
                                                     @NonNull String referenceInfo,
                                                     @NonNull Site site) {
    Preconditions.checkState(escapedContentTypes != null, "#afterPropertiesSet has not been called");
    Content linkable = resolveLinkable(referenceInfo, site);

    // If linkable is a nav element, return (ctx:linkable,content:linkable)
    if (asBean(linkable) instanceof Navigation) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("SearchTerm externalRef resolved context");
      }
      return new LinkableAndNavigation(linkable, linkable);
    }

    return linkable != null ? new LinkableAndNavigation(linkable, null) : null;
  }

  // --- internal ---------------------------------------------------

  @Nullable
  private Content resolveLinkable(@NonNull String referenceInfo, @NonNull Site site) {
    String searchTerm = referenceInfo.trim();
    return cacheForSeconds > 0
            ? cache.get(new ResolveLinkableKey(this, searchTerm, site, cacheForSeconds))
            : resolveLinkableUncached(searchTerm, site);
  }

  @Nullable
  private Content resolveLinkableUncached(@NonNull String searchTerm, @NonNull Site site) {
    Content context = getNavigationContext(site);
    if (context == null) {
      LOG.warn("Cannot find navigation with configured segment path '{}' for site {}", segmentPath, site.getName());
      return getFallbackLinkable(site);
    }

    SearchQueryBean query = new SearchQueryBean();
    query.setSearchHandler(SearchQueryBean.SEARCH_HANDLER.DYNAMICCONTENT);
    query.setNotSearchableFlagIgnored(true);
    query.addFilter(Condition.is(SearchConstants.FIELDS.DOCUMENTTYPE, Value.anyOf(escapedContentTypes)));
    query.addFilter(createContextSearchCondition(context));
    query.setQuery(field + ':' + escapeLiteralForSearch(searchTerm));

    SearchResultBean searchResult = searchResultFactory.createSearchResultUncached(query);
    Optional<ContentBean> result = FluentIterable.from(searchResult.getHits()).filter(ContentBean.class).first();
    if (result.isPresent()) {
      return result.get().getContent();
    }

    LOG.info("No content found for site {} that matches the search term '{}'. Will use the context '{}' instead.", site.getName(), searchTerm, context.getName());
    return context;
  }

  /**
   * Returns the navigation context configured with {@link #setSegmentPath(String)} relative to the
   * {@link com.coremedia.cap.multisite.Site#getSiteRootDocument() root document} of the given site.
   *
   * @param site site
   * @return navigation, null if not found
   */
  @Nullable
  private Content getNavigationContext(@NonNull Site site) {
    Content context = site.getSiteRootDocument();
    if (context == null) {
      return null;
    }

    QueryService queryService = contentRepository.getQueryService();
    Iterator<String> it = segments.iterator();
    while (it.hasNext() && context != null) {
      String segment = it.next();
      Collection<Content> children = navigationTreeRelation.getChildrenOf(context);
      context = queryService.getContentFulfilling(children, QUERY_NAVIGATION_WITH_SEGMENT, segment);
    }
    return context;
  }


  /**
   * Returns a fallback linkable that is returned by {@link #resolveExternalRef} if no match was found for a given
   * search term or null for no fallback.
   *
   * <p>The default implementation returns the given site's
   * {@link com.coremedia.cap.multisite.Site#getSiteRootDocument() root document}.
   *
   * @param site the site to resolve the reference in
   * @return fallback linkable or null
   */
  @Nullable
  @VisibleForTesting
  Content getFallbackLinkable(Site site) {
    return site.getSiteRootDocument();
  }

  /**
   * Creates a search {@link com.coremedia.blueprint.cae.search.Condition} to find content beans below a context.
   *
   * @param context the navigation context
   * @return search condition
   */
  @NonNull
  private Condition createContextSearchCondition(@NonNull Content context) {
    List<Content> contents = navigationTreeRelation.pathToRoot(context);
    StringBuilder sb = new StringBuilder();
    for (Content content : contents) {
      sb.append("\\/");
      sb.append(IdHelper.parseContentId(content.getId()));
    }
    return Condition.is(SearchConstants.FIELDS.NAVIGATION_PATHS, Value.exactly(sb.toString()));
  }

  @NonNull
  private static String escapeLiteralForSearch(@NonNull String literal) {
    return '"' + CharMatcher.is('"').replaceFrom(literal, "\\\"") + '"';
  }

  private static class ResolveLinkableKey extends CacheKey<Content> {

    private final SearchTermExternalReferenceResolver resolver;
    private final String searchTerm;
    private final Site site;
    private final int cacheForSeconds;

    public ResolveLinkableKey(@NonNull SearchTermExternalReferenceResolver resolver,
                              @NonNull String searchTerm,
                              @NonNull Site site,
                              int cacheForSeconds) {
      Preconditions.checkArgument(cacheForSeconds > 0);
      this.resolver = resolver;
      this.searchTerm = searchTerm;
      this.site = site;
      this.cacheForSeconds = cacheForSeconds;
    }

    @Override
    public Content evaluate(Cache cache) {
      // we're using a timed dependency here because real dependencies are not available for search engine queries
      Cache.cacheFor(cacheForSeconds, TimeUnit.SECONDS);
      Cache.disableDependencies();
      try {
        return resolver.resolveLinkableUncached(searchTerm, site);
      } finally {
        Cache.enableDependencies();
      }
    }

    @Override
    public String cacheClass(Cache cache, Content value) {
      return CACHE_CLASS;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      ResolveLinkableKey that = (ResolveLinkableKey) o;
      return resolver.equals(that.resolver) && searchTerm.equals(that.searchTerm) && site.equals(that.site);
    }

    @Override
    public int hashCode() {
      return Objects.hash(resolver, searchTerm, site);
    }
  }

}
