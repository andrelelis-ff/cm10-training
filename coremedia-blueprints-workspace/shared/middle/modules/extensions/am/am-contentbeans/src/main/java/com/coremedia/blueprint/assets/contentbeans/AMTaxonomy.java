package com.coremedia.blueprint.assets.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;

/**
 * <p>
 * Represents the document type {@link #NAME AMTaxonomy}.
 * </p>
 *
 * @cm.template.api
 */
public interface AMTaxonomy extends CMTaxonomy {

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'AMTaxonomy'.
   */
  String NAME = "AMTaxonomy";

  /**
   * Name of the document property 'assetThumbnail'
   */
  String ASSET_THUMBNAIL = "assetThumbnail";

  /**
   * Returns the value of the document property {@link #ASSET_THUMBNAIL}.
   *
   * @return the value of the document property {@link #ASSET_THUMBNAIL}
   * @cm.template.api
   */
  AMAsset getAssetThumbnail();
}
