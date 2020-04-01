package com.coremedia.blueprint.assets.contentbeans;

import com.coremedia.cap.common.CapBlobRef;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Represents a single rendition defined in an {@link AMAsset} bean.
 *
 * @cm.template.api
 */
public interface AMAssetRendition {

  /**
   * The asset the rendition is stored in.
   *
   * @return the asset the rendition is stored in.
   * @cm.template.api
   */
  @NonNull
  AMAsset getAsset();

  /**
   * The name of the rendition which is also the name of the document type property which
   * holds the rendition's blob.
   *
   * @return name of the rendition which is also the name of the document type property
   * @cm.template.api
   */
  @NonNull
  String getName();

  /**
   * Returns the size of the rendition's blob in bytes
   *
   * @return the size of the rendition's blob in bytes
   * @cm.template.api
   */
  int getSize();

  /**
   * Returns the type (Mime Subtype) of the rendition's blob or null
   *
   * @return the type (Mime Subtype) of the rendition's blob or null
   * @cm.template.api
   */
  @Nullable
  String getMimeType();

  /**
   * Returns the rendition's blob or null
   *
   * @return the rendition's blob or null
   * @cm.template.api
   */
  @Nullable
  CapBlobRef getBlob();

  /**
   * Check if the rendition is published i.e. the rendition should be displayed in the download portal
   *
   * @return {@code true} if the rendition is published otherwise {@code false}
   */
  boolean isPublished();
}
