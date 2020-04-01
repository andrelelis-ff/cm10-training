package com.coremedia.blueprint.assets.contentbeans;

import com.coremedia.cap.common.Blob;

/**
 * Base class for beans of document type "AMPictureAsset".
 */
public abstract class AMPictureAssetBase extends AMAssetImpl implements AMPictureAsset {

  protected Blob getWeb() {
    return getContent().getBlobRef(WEB);
  }

  protected Blob getPrint() {
    return getContent().getBlobRef(PRINT);
  }
}
