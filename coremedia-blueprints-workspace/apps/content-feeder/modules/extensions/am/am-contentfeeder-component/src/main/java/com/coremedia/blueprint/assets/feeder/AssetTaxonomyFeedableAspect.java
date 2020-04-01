package com.coremedia.blueprint.assets.feeder;

import com.coremedia.cap.feeder.FeedableAspect;

/**
 * {@link FeedableAspect} for updating taxonomy data in index documents.
 */
public enum AssetTaxonomyFeedableAspect implements FeedableAspect {

  ASSET_TAXONOMY_IDS("assetTaxonomyIds");

  private final String id;

  AssetTaxonomyFeedableAspect(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }

}
