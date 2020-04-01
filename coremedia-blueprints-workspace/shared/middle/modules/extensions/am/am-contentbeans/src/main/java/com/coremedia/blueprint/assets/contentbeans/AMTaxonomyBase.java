package com.coremedia.blueprint.assets.contentbeans;

import com.coremedia.blueprint.cae.contentbeans.CMTaxonomyImpl;
import com.coremedia.cap.content.Content;

/**
 * Base class for beans of document type "AMTaxonomy".
 */
public abstract class AMTaxonomyBase extends CMTaxonomyImpl implements AMTaxonomy {

  @Override
  public AMAsset getAssetThumbnail() {
    Content taxonomyPreviewContent = getContent().getLink(AMTaxonomy.ASSET_THUMBNAIL);
    return createBeanFor(taxonomyPreviewContent, AMAsset.class);
  }
}
