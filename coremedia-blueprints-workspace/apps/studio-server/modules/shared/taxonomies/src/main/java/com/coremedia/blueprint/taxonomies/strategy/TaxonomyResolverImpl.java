package com.coremedia.blueprint.taxonomies.strategy;

import com.coremedia.blueprint.taxonomies.Taxonomy;
import com.coremedia.blueprint.taxonomies.TaxonomyResolver;
import com.coremedia.blueprint.taxonomies.cycleprevention.TaxonomyCycleValidator;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.rest.cap.content.search.SearchService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import static com.coremedia.blueprint.taxonomies.strategy.TaxonomyStrategiesCacheKey.toTaxonomyStrategyKey;
import static com.google.common.base.Strings.emptyToNull;

/**
 * Concrete implementation of the ITaxonomyResolver.
 */
public class TaxonomyResolverImpl implements TaxonomyResolver {

  private final ContentRepository contentRepository;
  private final SearchService searchService;
  private final TaxonomyCycleValidator taxonomyCycleValidator;
  private final Map<String, String> aliasMapping;
  private final SitesService sitesService;
  private final String contentType;
  private final String siteConfigPath;
  private final String globalConfigPath;

  @SuppressWarnings({"ConstructorWithTooManyParameters", "squid:S00107"})
  public TaxonomyResolverImpl(@NonNull SitesService sitesService,
                              @NonNull ContentRepository contentRepository,
                              @NonNull SearchService searchService,
                              @NonNull TaxonomyCycleValidator taxonomyCycleValidator,
                              @NonNull Map<String, String> aliasMapping,
                              @NonNull String contentType,
                              @NonNull String siteConfigPath,
                              @NonNull String globalConfigPath) {
    this.sitesService = sitesService;
    this.contentRepository = contentRepository;
    this.searchService = searchService;
    this.taxonomyCycleValidator = taxonomyCycleValidator;
    this.aliasMapping = aliasMapping;
    this.contentType = contentType;
    this.siteConfigPath = siteConfigPath;
    this.globalConfigPath = globalConfigPath;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Collection<Taxonomy> getTaxonomies() {
    return new ArrayList<>(getStrategies().values());
  }

  private Map<String, Taxonomy<Content>> getStrategies() {
    TaxonomyStrategiesCacheKey cacheKey = new TaxonomyStrategiesCacheKey(
            contentRepository,
            sitesService,
            searchService,
            taxonomyCycleValidator,
            contentType,
            siteConfigPath,
            globalConfigPath
    );
    return contentRepository.getConnection().getCache().get(cacheKey);
  }

  @SuppressWarnings("rawtypes")
  @Override
  @Nullable
  public Taxonomy getTaxonomy(String siteId, String taxonomyId) {
    return findTaxonomy(siteId, taxonomyId);
  }

  /**
   * Recursive search for the taxonomy strategy matching the given id and site.
   * Lookup:
   * <ol>
   *   <li>Lookup taxonomy for the site and (taxonomy) id</li>
   *   <li>Lookup common taxonomy, ignoring site value</li>
   *   <li>Lookup alias mapping</li>
   * </ol>
   *
   * @param siteId     The site id the taxonomy is working on or {@code null} or empty if it is a global tree.
   * @param taxonomyId The id of the tree
   * @return The administrating object for the taxonomy tree.
   */
  @Nullable
  private Taxonomy<Content> findTaxonomy(@Nullable String siteId, String taxonomyId) {
    String normalizedSiteId = emptyToNull(siteId);
    Map<String, Taxonomy<Content>> strategies = getStrategies();
    Taxonomy<Content> strategy = strategies.get(toTaxonomyStrategyKey(taxonomyId, normalizedSiteId));

    // it's most probably that the first lookup fails, means that a taxonomy belongs to a site but
    // the site does not define a taxonomy of type XY of its own, so search for the common one, using site=null value
    if (strategy == null && normalizedSiteId != null) {
      strategy = strategies.get(toTaxonomyStrategyKey(taxonomyId, null));
    }
    if (strategy == null) {
      // still, not found? ok then try an alias next...
      Collection<String> seenAlias = new HashSet<>();
      seenAlias.add(taxonomyId);
      // null: Leave as soon as there is no alias anymore.
      seenAlias.add(null);
      String mappedTaxonomy = taxonomyId;

      while (!seenAlias.contains(aliasMapping.get(mappedTaxonomy))) {
        mappedTaxonomy = aliasMapping.get(mappedTaxonomy);
        seenAlias.add(mappedTaxonomy);

        strategy = strategies.get(toTaxonomyStrategyKey(mappedTaxonomy, normalizedSiteId));
        //if site (e.g. querySubject+media) is set, try to find root taxonomy and ignore the site name
        if (strategy == null && normalizedSiteId != null) {
          strategy = strategies.get(toTaxonomyStrategyKey(mappedTaxonomy, null));
        }
        if (strategy != null) {
          break;
        }
      }
    }
    return strategy;
  }
}
