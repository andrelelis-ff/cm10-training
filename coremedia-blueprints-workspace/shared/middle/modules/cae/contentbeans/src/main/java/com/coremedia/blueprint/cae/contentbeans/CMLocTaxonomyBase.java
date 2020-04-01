package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMLocTaxonomy;
import com.coremedia.blueprint.common.util.LatitudeLongitudeUtil;
import com.coremedia.cae.aspect.Aspect;

import java.util.List;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMLocTaxonomy.
 * Should not be changed.
 */
public abstract class CMLocTaxonomyBase extends CMTaxonomyImpl implements CMLocTaxonomy {

  @Override
  public abstract CMLocTaxonomy getParent();

  /**
   * Returns the value an aspect by name}.
   *
   * @return a list of {@link CMLocTaxonomy} object
   *
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings({"unchecked"})
  public Map<String, ? extends Aspect<? extends CMLocTaxonomy>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMLocTaxonomy>>) super.getAspectByName();
  }

  /**
   * Returns all aspects.
   *
   * @return a list of {@link CMLocTaxonomy} object
   *
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings({"unchecked"})
  public List<? extends Aspect<? extends CMLocTaxonomy>> getAspects() {
    return (List<? extends Aspect<? extends CMLocTaxonomy>>) super.getAspects();
  }

  /**
   * Returns the value of the document property {@link #POSTCODE}.
   *
   * @return the value of the document property {@link #POSTCODE}
   */
  @Override
  public String getPostcode() {
    return getContent().getString(POSTCODE);
  }

  /**
   * Returns the value of the document property {@link #LATITUDE_LONGITUDE}.
   *
   * @return the value of the document property {@link #LATITUDE_LONGITUDE}
   */
  @Override
  public String getLatitudeLongitude() {
    String latitudeLongitude = getContent().getString(LATITUDE_LONGITUDE);
    return LatitudeLongitudeUtil.validate(latitudeLongitude) == LatitudeLongitudeUtil.Result.VALID ? latitudeLongitude : null;
  }
}
