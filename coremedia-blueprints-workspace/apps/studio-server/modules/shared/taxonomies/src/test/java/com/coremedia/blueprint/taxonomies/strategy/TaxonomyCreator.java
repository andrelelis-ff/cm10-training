package com.coremedia.blueprint.taxonomies.strategy;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.TestInfo;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Utility class to create contents required for taxonomy tests.
 */
@DefaultAnnotation(NonNull.class)
public class TaxonomyCreator {
  private static final String NAME_TEMPLATE = "{3} ({1})";
  public static final String CM_TAXONOMY = "CMTaxonomy";
  public static final String SITE_CONFIG_PATH = "Options/";
  public static final String GLOBAL_CONFIG_PATH = "/Settings";

  private final ContentRepository contentRepository;
  private final SitesService sitesService;

  public TaxonomyCreator(ContentRepository contentRepository,
                         SitesService sitesService) {
    this.contentRepository = contentRepository;
    this.sitesService = sitesService;
  }

  public void createGlobalSubjectTaxonomy(String id) {
    Content subjectFolder = contentRepository.createSubfolders("/Settings/Taxonomies/Subject");
    ContentType taxonomyType = requireContentType(CM_TAXONOMY);
    taxonomyType.createByTemplate(subjectFolder, id, NAME_TEMPLATE, Map.of(
            "value", id
    ));
  }

  public void createSiteSpecificSubjectTaxonomy(Site site, String id) {
    Content subjectFolder = contentRepository.createSubfolders(site.getSiteRootFolder(), "Options/Taxonomies/Subject");
    ContentType taxonomyType = requireContentType(CM_TAXONOMY);
    taxonomyType.createByTemplate(subjectFolder, id, NAME_TEMPLATE, Map.of(
            "value", id
    ));
  }

  public Site createSite(TestInfo testInfo) {
    return createSite("", testInfo);
  }

  public Site createSite(String idPrefix, TestInfo testInfo) {
    String id = idPrefix + testInfo.getTestMethod().map(Method::getName).orElseThrow();

    Content siteRootFolder = contentRepository.createSubfolders(format("/Sites/%s", id));
    ContentType siteIndicatorType = requireContentType("CMSite");
    ContentType rootDocumentType = requireContentType("CMChannel");

    Content rootDocument = rootDocumentType.createByTemplate(siteRootFolder, id + "_root", NAME_TEMPLATE, Map.of());
    rootDocument.checkIn();

    String siteId = id.substring(0, Math.min(id.length(), 32));
    Content siteIndicator = siteIndicatorType.createByTemplate(siteRootFolder, id, NAME_TEMPLATE, Map.of(
            "id", siteId,
            "locale", Locale.US.toLanguageTag(),
            "name", id.substring(0, Math.min(id.length(), 64)),
            "root", List.of(rootDocument)
    ));
    siteIndicator.checkIn();

    return requireNonNull(sitesService.getSite(siteId), "Failed to create and retrieve site with id " + siteId);
  }

  private ContentType requireContentType(String idOrName) {
    return requireNonNull(contentRepository.getContentType(idOrName), () -> "Content Type does not exist: " + idOrName);
  }
}
