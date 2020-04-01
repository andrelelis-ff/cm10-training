package com.coremedia.blueprint.taxonomies;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.rest.cap.content.search.SearchService;
import com.coremedia.rest.cap.content.search.SearchServiceResult;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Common utility methods for taxonomies.
 */
public final class TaxonomyUtil {

  private static final String CM_TAXONOMY_TYPE_IDENTIFIER = "CMTaxonomy";

  /**
   * Hide Utility Class Constructor
   */
  private TaxonomyUtil() {
  }

  private static final String CONTENT_ID_PREFIX = "coremedia:///cap/";

  /**
   * Escapes special characters in the document name and replaces them with wildcards.
   *
   * @param name The document name that should be searched/filtered for.
   * @return The formatted search pattern.
   */
  @NonNull
  public static String formatQuery(@NonNull String name) {
    String result = '*' + name.replaceAll("-", " ");
    return result.endsWith(" ") ? result : result + '*';
  }

  /**
   * Determines if the given content is of type CMTaxonomy.
   *
   * @param tax content which might be a CMTaxonomy
   * @param contentType content type of the taxonomy
   * @return true if the content is a taxonomy, otherwise false
   */
  public static boolean isTaxonomy(@NonNull Content tax, ContentType contentType) {
    return tax.getType().isSubtypeOf(contentType);
  }

  @NonNull
  public static List<Content> search(@NonNull SearchService searchService,
                                     @Nullable Content folder,
                                     @NonNull ContentType type,
                                     @Nullable String query, int limit) {
    List<ContentType> types = new ArrayList<>();
    types.add(type);
    return search(searchService, folder, types, query, limit);
  }

  /**
   * Recursive call to collect all taxonomies for a folder.
   */
  @NonNull
  public static List<Content> search(@NonNull SearchService searchService,
                                     @Nullable Content folder,
                                     @NonNull Collection<ContentType> types,
                                     @Nullable String query, int limit) {
    SearchServiceResult result = searchService.search(query, limit,
            new ArrayList<>(),
            folder,
            true,
            types,
            true,
            Collections.singletonList("isdeleted:false"),
            new ArrayList<>(),
            new ArrayList<>());
    return result.getHits();
  }

  @NonNull
  public static String getRestIdFromCapId(@NonNull String ref) {
    return "content/" + ref.substring(ref.lastIndexOf('/') + 1);
  }

  @NonNull
  public static String asContentId(@NonNull String nodeRef) {
    return CONTENT_ID_PREFIX + nodeRef;
  }

  /**
   * Formats the content id to a taxonomy node id
   *
   * @param contentId The cap id to format the node id for.
   * @return The formatted node id.
   */
  @NonNull
  public static String asNodeRef(@NonNull String contentId) {
    return contentId.substring(CONTENT_ID_PREFIX.length());
  }
}
