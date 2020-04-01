package com.coremedia.blueprint.personalization.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMDynamicList;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.xml.Markup;

import java.util.List;

/**
 * Generated interface for beans of document type "CMSelectionRules".
 *
 * The bean corresponding to the <code>CMSelectionRules</code> document type. It selects its
 * content by applying the selection rules stored in the associated document to the
 * active user's profile.
 */
public interface CMSelectionRules extends CMDynamicList<CMTeasable> {

  /*
   * DEVELOPER NOTE
   * Change the methods to narrow the public interface
   * of the {@link com.coremedia.blueprint.personalization.contentbeans.CMSelectionRulesImpl} implementation bean.
   */

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMSelectionRules'
   */
  String CONTENTTYPE_CMSELECTIONRULES = "CMSelectionRules";

  /**
   * Returns the value of the document property "defaultContent"
   * @return the value
   */
  List<CMTeasable> getDefaultContent();

  /**
   * Returns the value of the document property "rules"
   * @return the value
   */
  Markup getRules();

  /**
   * Returns the value of the document property "master"
   * @return the value
   */
  @Override
  CMSelectionRules getMaster();
}
