package com.coremedia.blueprint.common.contentbeans;

/**
 * <p>
 * CMLocTaxonomy beans allow for a categorization of contents by
 * geographical attributes.
 * </p>
 * <p>
 * Represents document type {@link #NAME CMLocTaxonomy}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMLocTaxonomy extends CMTaxonomy {

  @Override
  CMLocTaxonomy getParent();

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMLocTaxonomy'.
   */

  String NAME = "CMLocTaxonomy";
  /*========================================================================================================
      Getters will go here
  =========================================================================================================*/

  /**
   * Name of the document property 'duration'.
   */
  String POSTCODE = "postcode";

  /**
   * Returns the value of the document property {@link #POSTCODE}.
   *
   * @return the value of the document property {@link #POSTCODE}
   */
  String getPostcode();

  /**
   * Name of the document property 'latitudeLongitude'.
   */
  String LATITUDE_LONGITUDE = "latitudeLongitude";

  /**
   * Returns the value of the document property {@link #LATITUDE_LONGITUDE}.
   *
   * @return the value of the document property {@link #LATITUDE_LONGITUDE}
   */
  String getLatitudeLongitude();

  /**
   * Returns the latitude value of the document property {@link #LATITUDE_LONGITUDE}.
   *
   * @return the latitude value of the document property {@link #LATITUDE_LONGITUDE}
   */
  String getLatitude();

  /**
   * Returns the longitude value of the document property {@link #LATITUDE_LONGITUDE}.
   *
   * @return the longitude value of the document property {@link #LATITUDE_LONGITUDE}
   */
  String getLongitude();
}
