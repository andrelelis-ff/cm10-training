package com.coremedia.livecontext.asset.util;

import java.util.List;

/**
 * A container for references to assets in the settings of a content.
 */
public interface AssetReferences {
  /**
   * Used references. Can match the origin references.
   *
   * @return list of used references
   */
  List<String> getReferences();

  /**
   * References derived from xmp or other assets.
   * Stable until the xmp properties or the list of other assets changes
   *
   * @return list of origin references
   */
  List<String> getOriginReferences();

  /**
   * Return if the references are inherited (from xmp or other assets) or not.
   *
   * @return true if the references are inherited, otherwise false.
   */
  boolean isInherit();
}
