package com.coremedia.blueprint.assets.cae;

import com.coremedia.blueprint.assets.contentbeans.AMTaxonomy;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * This class represents a single subcategory of an {@link CategoryOverview}. It is basically a means
 * to enrich the {@link AMTaxonomy} with additional information about the included assets from the
 * search query.
 *
 * @see CategoryOverview#getSubcategories()
 *
 * @cm.template.api
 */
public class Subcategory {

  private AMTaxonomy category = null;

  private long assetsInCategory = 0;


  /**
   * Creates an object of the {@link Subcategory}.
   * @param category the {@link AMTaxonomy} that represents the subcategory
   * @param assetsInCategory the number of assets within or below the category (val > 0)
   */
  public Subcategory(@NonNull AMTaxonomy category, long assetsInCategory) {
    this.category = category;
    if (assetsInCategory > 0) {
      this.assetsInCategory = assetsInCategory;
    }
  }

  /**
   * Returns the {@link AMTaxonomy contentbean} that represents the subcategory
   * @return the {@link AMTaxonomy contentbean} that represents the subcategory
   */
  @NonNull
  public AMTaxonomy getCategory() {
    return category;
  }

  /**
   * Returns the number of assets within this category.
   * @return the number of assets within this category.
   */
  public long getAssetsInCategory() {
    return assetsInCategory;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Subcategory)) {
      return false;
    }

    Subcategory that = (Subcategory) o;

    return assetsInCategory == that.assetsInCategory && category.equals(that.category);
  }

  @Override
  public int hashCode() {
    int result = category.hashCode();
    result = 31 * result + (int) (assetsInCategory ^ (assetsInCategory >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
            "category=" + category +
            ", assetsInCategory=" + assetsInCategory +
            '}';
  }
}
