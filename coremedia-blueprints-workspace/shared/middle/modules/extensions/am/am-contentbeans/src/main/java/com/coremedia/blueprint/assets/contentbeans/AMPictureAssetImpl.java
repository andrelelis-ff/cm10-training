package com.coremedia.blueprint.assets.contentbeans;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Extension class for beans of document type "AMPictureAsset".
 */
public class AMPictureAssetImpl extends AMPictureAssetBase {

  @NonNull
  @Override
  public List<AMAssetRendition> getRenditions() {
    List<AMAssetRendition> result = new ArrayList<>();

    result.addAll(super.getRenditions());
    result.add(getRendition(AMPictureAsset.WEB));
    result.add(getRendition(AMPictureAsset.PRINT));

    return result;
  }

}
