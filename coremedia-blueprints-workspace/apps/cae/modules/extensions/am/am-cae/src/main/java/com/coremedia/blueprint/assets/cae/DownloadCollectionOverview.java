package com.coremedia.blueprint.assets.cae;

import com.coremedia.blueprint.assets.contentbeans.AMAssetRendition;

import java.util.ArrayList;
import java.util.List;

/**
 * @cm.template.api
 */
public class DownloadCollectionOverview implements DownloadPortalContext {

  private final List<AMAssetRendition> renditions;

  public DownloadCollectionOverview(List<AMAssetRendition> renditions) {
    this.renditions = new ArrayList<>(renditions);
  }

  /**
   * @cm.template.api
   */
  public List<AMAssetRendition> getRenditions() {
    return renditions;
  }

  @Override
  public String getSearchTerm() {
    return "";
  }
}
