package com.coremedia.blueprint.personalization.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMLocalized;
import com.coremedia.cap.common.Blob;

import java.util.Map;

/**
 * Generated interface for beans of document type "CMUserProfile".
 *
 * Dummy bean for the CMUserProfile document type. Instances of the CMUserProfile doctype
 * contain internal data that is not supposed to be visible to site visitors, but the editor
 * preview nonetheless requires a valid CAE bean and corresponding template.
 */
public interface CMUserProfile extends CMLocalized {

  /*
   * DEVELOPER NOTE
   * Change the methods to narrow the public interface
   * of the {@link com.coremedia.blueprint.personalization.contentbeans.CMUserProfileImpl} implementation bean.
   */

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMUserProfile'
   */
  String CONTENTTYPE_CMUSERPROFILE = "CMUserProfile";

  /**
   * Name of the document property 'profileExtensions'
   */
  String PROFILE_EXTENSIONS = "profileExtensions";

  /**
   * Name of the document property 'profileSettings'
   */
  String PROFILE_SETTINGS = "profileSettings";

  /**
   * Returns the value of the document property "profileSettings"
   * @return the value
   */
  Blob getProfileSettings();

  /**
   * Returns the value of the document property "profileExtensions"
   * @return the value
   */
  Map<String, Object> getProfileExtensions();

  /**
   * Returns the value of the document property "master"
   * @return the value
   */
  @Override
  CMUserProfile getMaster();
}
