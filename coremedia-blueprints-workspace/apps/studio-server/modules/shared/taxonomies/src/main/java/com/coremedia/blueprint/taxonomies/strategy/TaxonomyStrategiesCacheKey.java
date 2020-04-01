package com.coremedia.blueprint.taxonomies.strategy;

import com.coremedia.blueprint.taxonomies.Taxonomy;
import com.coremedia.blueprint.taxonomies.cycleprevention.TaxonomyCycleValidator;
import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.coremedia.cap.common.CapSession;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.rest.cap.content.search.SearchService;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Cache Key for accessing taxonomies. Invalid taxonomies will be removed
 * from result.
 */
@DefaultAnnotation(NonNull.class)
public class TaxonomyStrategiesCacheKey extends CacheKey<Map<String, Taxonomy<Content>>> {
  private static final Logger LOG = getLogger(lookup().lookupClass());

  /**
   * Context ID for global taxonomies. ID contains a character, which is unlikely to
   * be part of site IDs, which is, that all other context IDs are site IDs.
   */
  private static final String GLOBAL_CONTEXT_ID = "glo\nbal";
  /**
   * Pattern for taxonomy keys. First string: taxonomy ID, second string:
   * context ID (e.g., the corresponding site).
   */
  private static final String TAXONOMY_STRATEGY_KEY_PATTERN = "%s_%s";
  /**
   * Hard-wired folder name to search taxonomies in.
   */
  private static final String TAXONOMY_FOLDER_NAME = "Taxonomies";

  private final ContentRepository contentRepository;
  private final SitesService sitesService;
  private final String contentType;
  private final String siteConfigPath;
  private final String globalConfigPath;
  private final CreateTaxonomyFunction createTaxonomyFunction;

  public TaxonomyStrategiesCacheKey(ContentRepository contentRepository,
                                    SitesService sitesService,
                                    SearchService searchService,
                                    TaxonomyCycleValidator taxonomyCycleValidator,
                                    String contentType,
                                    String siteConfigPath,
                                    String globalConfigPath) {
    this(
            contentRepository,
            sitesService,
            contentType,
            siteConfigPath,
            globalConfigPath,
            (rootFolder, siteId, type) -> new DefaultTaxonomy(
                    rootFolder,
                    siteId,
                    type,
                    contentRepository,
                    searchService,
                    taxonomyCycleValidator
            )
    );
  }

  public TaxonomyStrategiesCacheKey(ContentRepository contentRepository,
                                    SitesService sitesService,
                                    String contentType,
                                    String siteConfigPath,
                                    String globalConfigPath,
                                    CreateTaxonomyFunction createTaxonomyFunction) {
    this.contentRepository = requireNonNull(contentRepository);
    this.sitesService = requireNonNull(sitesService);
    this.contentType = requireNonNull(contentType);
    this.siteConfigPath = requireNonNull(siteConfigPath);
    this.globalConfigPath = requireNonNull(globalConfigPath);
    this.createTaxonomyFunction = requireNonNull(createTaxonomyFunction);
  }

  @Override
  public Map<String, Taxonomy<Content>> evaluate(Cache cache) {
    return evaluate(contentRepository.getContentType(contentType));
  }

  /**
   * Evaluate mapping for a given content type.
   *
   * @param taxonomyType content type.
   * @return grouped taxonomy strategies; empty, if content type is {@code null}, or no taxonomies have been found.
   */
  private Map<String, Taxonomy<Content>> evaluate(@Nullable ContentType taxonomyType) {
    if (taxonomyType == null) {
      LOG.warn("Cannot evaluate taxonomy strategies for unknown content type: {}. Provided taxonomies will be empty.", contentType);
      return Map.of();
    }

    LOG.debug("Evaluating taxonomy strategies for content type: {}", taxonomyType);

    Map<String, Taxonomy<Content>> result = new HashMap<>();

    CapSession oldSession = contentRepository.getConnection().getConnectionSession().activate();

    try {
      result.putAll(evaluateForSites(taxonomyType));
      result.putAll(evaluateGlobally(taxonomyType));
    } finally {
      oldSession.activate();
    }

    LOG.debug("Evaluated taxonomy strategies for content type {}: Found {} strategies.", taxonomyType, result.size());

    return result;
  }

  /**
   * Retrieve all taxonomy strategies from the global folder.
   *
   * @param taxonomyType content type of taxonomy.
   * @return grouped taxonomy strategies.
   */
  private Map<String, Taxonomy<Content>> evaluateGlobally(ContentType taxonomyType) {
    return evaluateFor(contentRepository.getChild(globalConfigPath), null, taxonomyType);
  }

  /**
   * Retrieve all taxonomy strategies from all sites.
   *
   * @param taxonomyType content type of taxonomy.
   * @return grouped taxonomy strategies.
   */
  private Map<String, Taxonomy<Content>> evaluateForSites(ContentType taxonomyType) {
    Map<String, Taxonomy<Content>> result = new HashMap<>();
    Set<Site> sites = sitesService.getSites();

    for (Site site : sites) {
      result.putAll(evaluateForSite(site, taxonomyType));
    }

    return result;
  }

  /**
   * Retrieve all taxonomy strategies for one given site.
   *
   * @param taxonomyType content type of taxonomy.
   * @return grouped taxonomy strategies.
   */
  private Map<String, Taxonomy<Content>> evaluateForSite(Site site, ContentType taxonomyType) {
    Content siteConfigFolder = site.getSiteRootFolder().getChild(siteConfigPath);
    return evaluateFor(siteConfigFolder, site.getId(), taxonomyType);
  }

  /**
   * Retrieve taxonomy strategies for the given context.
   * Taxonomies are expected below a folder named {@code Taxonomies} below the
   * given root folder.
   *
   * @param taxonomyRootFolder root folder for taxonomies; if {@code null}, empty, or not a folder an empty map will be returned.
   * @param taxonomyContextId  taxonomy context; typically the site ID, {@code null} for global context.
   * @param taxonomyType       content type for taxonomies.
   * @return grouped taxonomy strategies.
   */
  private Map<String, Taxonomy<Content>> evaluateFor(@Nullable Content taxonomyRootFolder,
                                                     @Nullable String taxonomyContextId,
                                                     ContentType taxonomyType) {
    if (taxonomyRootFolder == null || !taxonomyRootFolder.isFolder()) {
      return Map.of();
    }
    Content taxonomiesFolder = taxonomyRootFolder.getChild(TAXONOMY_FOLDER_NAME);
    if (taxonomiesFolder == null || !taxonomiesFolder.isFolder()) {
      return Map.of();
    }

    Map<String, Taxonomy<Content>> result = new HashMap<>();
    Set<Content> subfolders = taxonomiesFolder.getSubfolders();
    for (Content subfolder : subfolders) {
      Taxonomy<Content> taxonomy = createTaxonomyFunction.apply(subfolder, taxonomyContextId, taxonomyType);
      if (taxonomy.isValid()) {
        result.put(toTaxonomyStrategyKey(taxonomy), taxonomy);
      }
    }
    return result;
  }

  /**
   * Unique key for taxonomy strategy.
   *
   * @param taxonomy taxonomy to create the key for.
   * @return The key of the taxonomy.
   */
  private static String toTaxonomyStrategyKey(Taxonomy<?> taxonomy) {
    return toTaxonomyStrategyKey(taxonomy.getTaxonomyId(), taxonomy.getSiteId());
  }

  /**
   * Unique key for taxonomy strategy.
   *
   * @param taxonomyId        The id of the taxonomy
   * @param taxonomyContextId The id of the taxonomy context.
   * @return The key of the taxonomy.
   */
  static String toTaxonomyStrategyKey(String taxonomyId, @Nullable String taxonomyContextId) {
    String contextId = taxonomyContextId == null ? GLOBAL_CONTEXT_ID : taxonomyContextId;
    return format(TAXONOMY_STRATEGY_KEY_PATTERN, taxonomyId, contextId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TaxonomyStrategiesCacheKey that = (TaxonomyStrategiesCacheKey) o;
    return contentType.equals(that.contentType) &&
            siteConfigPath.equals(that.siteConfigPath) &&
            globalConfigPath.equals(that.globalConfigPath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(contentType, siteConfigPath, globalConfigPath);
  }
}
