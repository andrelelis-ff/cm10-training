package com.coremedia.blueprint.assets.cae;

import com.coremedia.blueprint.assets.contentbeans.AMAsset;
import com.coremedia.blueprint.assets.contentbeans.AMTaxonomy;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Map;

/**
 * Represents the combination of an asset and one of its categories.
 *
 * @cm.template.api
 */
public class AssetDetails implements DownloadPortalContext {

  private AMAsset asset;

  private AMTaxonomy category;

  private Map<String, String> metadataProperties;

  public AssetDetails(@NonNull AMAsset asset, @Nullable AMTaxonomy category) {
    this.asset = asset;
    this.category = category;
  }

  /**
   * @cm.template.api
   */
  @NonNull
  public AMAsset getAsset() {
    return asset;
  }

  /**
   * @cm.template.api
   */
  @Nullable
  public AMTaxonomy getCategory() {
    return category;
  }

  /**
   * @cm.template.api
   */
  @Nullable
  public Map<String, String> getMetadataProperties() {
    return metadataProperties;
  }

  public void setMetadataProperties(Map<String, String> metadataProperties) {
    this.metadataProperties = metadataProperties;
  }

  @Override
  public String getSearchTerm() {
    return "";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AssetDetails that = (AssetDetails) o;

    return asset.equals(that.asset) && (category == null ? that.category == null : category.equals(that.category));

  }

  @Override
  public int hashCode() {
    int result = asset.hashCode();
    result = 31 * result + (category != null ? category.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
            "asset=" + asset +
            ", category=" + category +
            '}';
  }

}
