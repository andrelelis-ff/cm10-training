package com.coremedia.blueprint.assets.cae;

import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * This model provides an overview of assets linked with the given {@link CMTaxonomy}.
 *
 * @cm.template.api
 */
public class TaxonomyOverview implements DownloadPortalContext {

  private CMTaxonomy taxonomy;

  /**
   * Creates an instance of the {@link TaxonomyOverview}
   *
   * @param taxonomy the selected {@link CMTaxonomy}
   */
  public TaxonomyOverview(@NonNull CMTaxonomy taxonomy) {
    this.taxonomy = taxonomy;
  }

  /**
   * Returns the {@link CMTaxonomy}
   *
   * @return the {@link CMTaxonomy}
   * @cm.template.api
   */
  @Nullable
  public CMTaxonomy getTaxonomy() {
    return taxonomy;
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
    if (!(o instanceof TaxonomyOverview)) {
      return false;
    }

    TaxonomyOverview that = (TaxonomyOverview) o;

    return !(taxonomy != null ? !taxonomy.equals(that.taxonomy) : that.taxonomy != null);
  }

  @Override
  public int hashCode() {
    return taxonomy != null ? taxonomy.hashCode() : 0;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
            "taxonomy=" + taxonomy +
            '}';
  }
}
