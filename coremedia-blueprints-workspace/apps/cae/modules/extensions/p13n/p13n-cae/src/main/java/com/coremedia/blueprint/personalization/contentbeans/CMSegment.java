package com.coremedia.blueprint.personalization.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMLocalized;
import com.coremedia.xml.Markup;

/**
 * Generated interface for beans of document type "CMSegment".
 *
 * The bean corresponding to the <code>CMSegment</code> document type. It selects its
 * content by applying the selection rules stored in the associated document to the
 * active user's profile.
 */
public interface CMSegment extends CMLocalized {

  /*
   * DEVELOPER NOTE
   * Change the methods to narrow the public interface
   * of the {@link com.coremedia.blueprint.personalization.contentbeans.CMSegmentImpl} implementation bean.
   */

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMSegment'
   */
  String CONTENTTYPE_CMSEGMENT = "CMSegment";

  /**
   * Returns the value of the document property "conditions"
   * @return the value
   */
  Markup getConditions();

  /**
   * Returns the value of the document property "master"
   * @return the value
   */
  @Override
  CMSegment getMaster();
}
