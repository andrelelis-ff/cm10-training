package com.coremedia.blueprint.assets.contentbeans;

import com.coremedia.cap.common.Blob;

/**
 * Base class for beans of document type "AMDocumentAsset".
 */
public class AMDocumentAssetBase extends AMAssetImpl implements AMDocumentAsset {

  protected Blob getDownload() {
    return getContent().getBlobRef(DOWNLOAD);
  }
}
