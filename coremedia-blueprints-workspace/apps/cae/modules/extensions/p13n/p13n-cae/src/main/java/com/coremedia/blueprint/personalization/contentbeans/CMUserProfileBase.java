package com.coremedia.blueprint.personalization.contentbeans;

import com.coremedia.blueprint.cae.contentbeans.CMLocalizedImpl;
import com.coremedia.cap.common.Blob;

import java.util.Map;

/**
 * Generated base class for beans of document type "CMUserProfile".
 *
 * Dummy bean for the CMUserProfile document type. Instances of the CMUserProfile doctype
 * contain internal data that is not supposed to be visible to site visitors, but the editor
 * preview nonetheless requires a valid CAE bean and corresponding template.
 */
public abstract class CMUserProfileBase extends CMLocalizedImpl implements CMUserProfile {

  /*
   * DEVELOPER NOTE
   * Change {@link com.coremedia.blueprint.personalization.contentbeans.CMUserProfileImpl} instead of this class.
   */

  /**
   * Returns the value of the document property "profileSettings"
   * @return the value of the document property "profileSettings"
   */
  @Override
  public Blob getProfileSettings() {
    return getContent().getBlobRef(PROFILE_SETTINGS);
  }

  /**
   * Returns the value of the document property "profileExtensions"
   *
   * @return the value of the document property "profileExtensions"
   */
  @Override
  public Map<String, Object> getProfileExtensions() {
    return getContentBeanFactory().createBeanMapFor(getContent().getStruct(PROFILE_EXTENSIONS));
  }

  /**
   * Returns the value of the document property "master"
   * @return the value of the document property "master"
   */
  @Override
  public CMUserProfile getMaster() {
    return (CMUserProfile)super.getMaster();
  }

}
