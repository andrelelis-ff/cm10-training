package com.coremedia.blueprint.feeder.cae.assets;

import com.coremedia.blueprint.assets.contentbeans.AMAsset;
import com.coremedia.blueprint.cae.search.SearchConstants;
import com.coremedia.cap.feeder.MutableFeedable;
import com.coremedia.cap.feeder.populate.FeedablePopulator;
import com.coremedia.objectserver.beans.ContentBean;

/**
 * FeedablePopulator that sets the notsearchable index field for AMAssets.
 */
public class AssetNotSearchableFeedablePopulator implements FeedablePopulator<ContentBean> {

  @Override
  public void populate(MutableFeedable feedable, ContentBean source) {
    if (feedable == null || source == null) {
      throw new IllegalArgumentException("mutableFeedable and source must not be null");
    }
    if (source instanceof AMAsset) {
      AMAsset asset = (AMAsset) source;
      // Do not use the validationService even if it checks this condition as well.
      // Some of its tests will introduce uncacheable dependencies which in turn will cause
      // the assets to be feeded again and again.
      boolean notSearchable = asset.getPublishedRenditions().isEmpty();
      feedable.setElement(SearchConstants.FIELDS.NOT_SEARCHABLE.toString(), notSearchable);
    }
  }

}
