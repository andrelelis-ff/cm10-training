package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMMedia;
import com.coremedia.cap.common.Blob;
import com.coremedia.transform.NamedTransformBeanBlobTransformer;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Generated extension class for immutable beans of document type " CMMedia".
 */
public abstract class CMMediaImpl extends CMMediaBase {
  private NamedTransformBeanBlobTransformer mediaTransformer;

  public void setMediaTransformer(NamedTransformBeanBlobTransformer mediaTransformer) {
    this.mediaTransformer = mediaTransformer;
  }

  /**
   * Return the value of the alt property or an empty String.
   * <p/>
   * In XHTML the img.alt attribute is required, so null would just cause
   * overhead in the templates.
   */
  @Override
  public String getAlt() {
    String alt = super.getAlt();
    return alt == null ? "" : alt;
  }

  @Override
  public Blob getTransformedData(String transformName) {

    if (getDisableCropping()) {
      return (Blob)getData();
    }
    if (mediaTransformer != null) {
      return mediaTransformer.transform(this, transformName);
    }
    return null;
  }

  @Override
  public boolean getDisableCropping() {
    return false;
  }


  @Override
  public List<CMMedia> getMedia() {
    List<CMMedia> media = super.getMedia();
    return isNotEmpty(media) ? media : Lists.newArrayList(this);
  }
}
