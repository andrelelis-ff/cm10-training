package com.coremedia.blueprint.cae.search;

/**
 * Common engine independent search parameters
 */
public class SearchConstants {

  /**
   * Field names in the index. Need to map Solr schema
   */
  public enum FIELDS {
    ID("id"),
    DOCUMENTTYPE("documenttype"),
    NAVIGATION_PATHS("navigationpaths"),
    NOT_SEARCHABLE("notsearchable"),
    SUBJECT_TAXONOMY("subjecttaxonomy"),
    LOCATION_TAXONOMY("locationtaxonomy"),
    TITLE("title"),
    TEASER_TITLE("teaserTitle"),
    TEASER_TEXT("teaserText"),
    KEYWORDS("keywords"),
    MODIFICATION_DATE("freshness"),
    CREATION_DATE("creationDate"),
    TEXTBODY("textbody"),
    SEGMENT("segment"),
    COMMERCE_ITEMS("commerceitems"),
    CONTEXTS("contexts"),
    AUTHORS("authors"),
    HTML_DESCRIPTION("htmlDescription"),
    EXTERNALLY_DISPLAYED_DATE("externallydisplayeddate");

    private String name;

    FIELDS(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

}
