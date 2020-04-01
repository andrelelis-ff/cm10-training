package com.coremedia.blueprint.feeder.populate;

import com.coremedia.cap.feeder.FeedableAspect;

/**
 * {@link FeedableAspect} for updating taxonomy data in index documents.
 */
public enum TaxonomyFeedableAspect implements FeedableAspect {

  SUBJECT_TAXONOMY_IDS("subjectTaxonomyIds"),
  LOCATION_TAXONOMY_IDS("locationTaxonomyIds");

  private final String id;

  TaxonomyFeedableAspect(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }

}
