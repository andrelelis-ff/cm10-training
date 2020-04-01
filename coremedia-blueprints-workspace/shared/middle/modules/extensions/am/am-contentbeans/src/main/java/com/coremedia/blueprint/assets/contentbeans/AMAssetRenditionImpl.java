package com.coremedia.blueprint.assets.contentbeans;

import com.coremedia.blueprint.assets.common.AMSettingKeys;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapBlobRef;
import com.coremedia.cap.struct.Struct;
import com.coremedia.objectserver.dataviews.AssumesIdentity;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Collections;
import java.util.Map;

public class AMAssetRenditionImpl implements AMAssetRendition, AssumesIdentity {

  private String renditionName;
  private AMAsset asset;

  /**
   * Do not use - only used for dataview caching
   */
  public AMAssetRenditionImpl() {
    // only used for dataview caching
  }

  public AMAssetRenditionImpl(@NonNull String renditionName, @NonNull AMAsset asset) {
    this.asset = asset;
    this.renditionName = renditionName;
  }

  @NonNull
  @Override
  public AMAsset getAsset() {
    return asset;
  }

  @NonNull
  @Override
  public String getName() {
    return renditionName;
  }

  @Override
  public int getSize() {
    Blob blob = getBlob();
    return blob == null ? 0 : getBlob().getSize();
  }

  @Override
  public String getMimeType() {
    Blob blob = getBlob();
    return blob == null ? null : getBlob().getContentType().toString();
  }

  @Nullable
  @Override
  public CapBlobRef getBlob() {
    return asset.getContent().getBlobRef(renditionName);
  }

  @Override
  public boolean isPublished() {
    Map<String, Object> metadata = getRenditionMetadata();
    Object oShow = metadata.get(AMSettingKeys.METADATA_RENDITION_SHOW_PROPERTY_NAME);
    return Boolean.TRUE.equals(oShow);
  }

  @NonNull
  private Map<String, Object> getRenditionMetadata() {
    Struct struct = asset.getContent().getStruct(AMAsset.METADATA);
    if (null != struct) {
      Object oRenditions = struct.get(AMSettingKeys.METADATA_RENDITIONS_PROPERTY_NAME);
      if (oRenditions instanceof Struct) {
        Object oRendition = ((Struct) oRenditions).get(getName());
        if (oRendition instanceof Struct) {
          return ((Struct)oRendition).getProperties();
        }
      }
    }
    return Collections.emptyMap();

  }

  @Override
  public void assumeIdentity(Object bean) {
    AMAssetRenditionImpl original = (AMAssetRenditionImpl)bean;
    this.renditionName = original.renditionName;
    this.asset = original.asset;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AMAssetRenditionImpl)) {
      return false;
    }

    AMAssetRenditionImpl that = (AMAssetRenditionImpl) o;

    return renditionName.equals(that.renditionName) && asset.equals(that.asset);

  }

  @Override
  public int hashCode() {
    int result = renditionName.hashCode();
    result = 31 * result + asset.hashCode();
    return result;
  }
}