package com.coremedia.blueprint.assets.contentbeans;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;

public class AMDocumentAssetImpl extends AMDocumentAssetBase {
  @NonNull
  @Override
  public List<AMAssetRendition> getRenditions() {
    List<AMAssetRendition> result = new ArrayList<>();

    result.addAll(super.getRenditions());
    result.add(getRendition(AMDocumentAsset.DOWNLOAD));

    return result;
  }

}