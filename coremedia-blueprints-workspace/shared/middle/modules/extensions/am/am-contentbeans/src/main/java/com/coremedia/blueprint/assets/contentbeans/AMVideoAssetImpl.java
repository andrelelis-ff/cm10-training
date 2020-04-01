package com.coremedia.blueprint.assets.contentbeans;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Extension class for beans of document type "AMVideoAsset".
 */
public class AMVideoAssetImpl extends AMVideoAssetBase {

  @NonNull
  @Override
  public List<AMAssetRendition> getRenditions() {
    List<AMAssetRendition> result = new ArrayList<>();

    result.addAll(super.getRenditions());
    result.add(getRendition(AMVideoAsset.WEB));

    return result;
  }

}
