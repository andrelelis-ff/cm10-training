package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMPicture;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.content.Content;

import java.util.ArrayList;
import java.util.List;

/**
 * Generated extension class for immutable beans of document type "CMSpinner".
 */
public class CMSpinnerImpl extends CMSpinnerBase {
  @Override
  public List<Blob> getData() {
    ArrayList<Blob> blobs = new ArrayList<>();
    List<Content> images = getContent().getLinks(SEQUENCE);
    for (Content image : images) {
      blobs.add(image.getBlobRef(CMPicture.DATA));
    }
    return blobs;
  }
}
