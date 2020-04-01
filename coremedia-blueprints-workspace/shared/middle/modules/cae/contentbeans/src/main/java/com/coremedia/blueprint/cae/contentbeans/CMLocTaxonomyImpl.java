package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.cae.search.SearchConstants;
import com.coremedia.blueprint.common.contentbeans.CMLocTaxonomy;
import org.apache.commons.lang3.StringUtils;

/**
 * Generated extension class for immutable beans of document type " CMLocTaxonomy".
 */
public class CMLocTaxonomyImpl extends CMLocTaxonomyBase {
  @Override
  public CMLocTaxonomy getParent() {
    return createBeanFor(getTreeRelation().getParentOf(this.getContent()), CMLocTaxonomy.class);
  }

  /**
   * Returns the latitude value of the document property {@link #LATITUDE_LONGITUDE}.
   *
   * @return the latitude value of the document property {@link #LATITUDE_LONGITUDE}
   */
  @Override
  public String getLatitude() {
    String[] result = StringUtils.split(super.getLatitudeLongitude(), ',');
    if (result == null || result.length != 2) {
      return null;
    }
    return result[0];
  }

  /**
   * Returns the longitude value of the document property {@link #LATITUDE_LONGITUDE}.
   *
   * @return the longitude value of the document property {@link #LATITUDE_LONGITUDE}
   */
  @Override
  public String getLongitude() {
    String[] result = StringUtils.split(super.getLatitudeLongitude(), ',');
    if (result == null || result.length != 2) {
      return null;
    }
    return result[1];
  }

  protected SearchConstants.FIELDS taxonomySearchField() {
    return SearchConstants.FIELDS.LOCATION_TAXONOMY;
  }
}
