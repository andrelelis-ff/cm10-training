package com.coremedia.blueprint.personalization.contentbeans;

import com.coremedia.blueprint.cae.contentbeans.CMLocalizedImpl;
import com.coremedia.xml.Markup;

/**
 * Generated base class for beans of document type "CMSegment".
 * 
 * The bean corresponding to the <code>CMSegment</code> document type. It selects its
 * content by applying the selection rules stored in the associated document to the
 * active user's profile.
 */
public abstract class CMSegmentBase extends CMLocalizedImpl implements CMSegment {

  /*
   * DEVELOPER NOTE
   * Change {@link com.coremedia.blueprint.personalization.contentbeans.CMSegmentImpl} instead of this class.
   */

  /**
   * Returns the value of the document property "conditions"
   * @return the value of the document property "conditions"
   */
  @Override
  public Markup getConditions() {
    return getContent().getMarkup("conditions");
  }

  /**
   * Returns the value of the document property "master"
   * @return the value of the document property "master"
   */
  @Override
  public CMSegment getMaster() {
    return (CMSegment)super.getMaster();
  }

}
