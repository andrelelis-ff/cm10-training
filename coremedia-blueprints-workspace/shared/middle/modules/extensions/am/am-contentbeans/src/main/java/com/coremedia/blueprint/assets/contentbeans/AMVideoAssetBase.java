package com.coremedia.blueprint.assets.contentbeans;

import com.coremedia.cap.common.Blob;

/**
 * Base class for beans of document type "AMVideoAsset".
 */
public abstract class AMVideoAssetBase extends AMAssetImpl implements AMVideoAsset {

  protected Blob getWeb() {
    return getContent().getBlobRef(WEB);
  }
}
