package com.coremedia.blueprint.feeder.cae.assets;

import com.coremedia.blueprint.assets.contentbeans.AMAsset;
import com.coremedia.blueprint.assets.contentbeans.AMAssetRendition;
import com.coremedia.blueprint.assets.contentbeans.AMDocumentAsset;
import com.coremedia.cap.feeder.MutableFeedable;
import com.coremedia.cap.feeder.TextParameters;
import com.coremedia.cap.feeder.populate.FeedablePopulator;
import com.coremedia.objectserver.beans.ContentBean;

import java.util.List;

/**
 *  A {@link FeedablePopulator} to index up to one rendition of an AMDocumentAsset that is downloadable.
 *  Only considers renditions that are marked as downloadable. If the "download" rendition is available and downloadable
 *  it will be used. Otherwise the "original" rendition will be used, also only if it is set and marked as downloadable.
 *  The extracted text is only added to the {@link TextParameters#TEXT_BODY textbody}.
 */
public class AssetDocumentBlobFeedablePopulator implements FeedablePopulator<ContentBean> {

  @Override
  public void populate(MutableFeedable feedable, ContentBean source) {
    if (feedable == null || source == null) {
      throw new IllegalArgumentException("mutableFeedable and source must not be null");
    }
    if (source instanceof AMDocumentAsset) {
      AMDocumentAsset asset = (AMDocumentAsset) source;
      List<AMAssetRendition> publishedRenditions = asset.getPublishedRenditions();
      AMAssetRendition originalRendition = null;
      AMAssetRendition downloadRendition = null;

      for (AMAssetRendition rendition: publishedRenditions) {
        if (AMAsset.ORIGINAL.equals(rendition.getName())) {
          originalRendition = rendition;
        } else  if (AMDocumentAsset.DOWNLOAD.equals(rendition.getName())) {
          downloadRendition = rendition;
        }
      }
      // text should be available in field textBody only
      if (null != downloadRendition) {
        feedable.setElement(null, downloadRendition.getBlob());
      } else if (null != originalRendition) {
        feedable.setElement(null, originalRendition.getBlob());
      }
    }
  }
}
