package com.coremedia.blueprint.personalization.contentbeans;

import com.coremedia.blueprint.cae.contentbeans.CMDynamicListImpl;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;

import com.coremedia.cap.content.Content;
import com.coremedia.xml.Markup;

import java.util.List;

/**
 * Generated base class for beans of document type "CMSelectionRules".
 * 
 * The bean corresponding to the <code>CMSelectionRules</code> document type. It selects its
 * content by applying the selection rules stored in the associated document to the
 * active user's profile.
 */
public abstract class CMSelectionRulesBase extends CMDynamicListImpl<CMTeasable> implements CMSelectionRules {

  /*
   * DEVELOPER NOTE
   * Change {@link com.coremedia.blueprint.personalization.contentbeans.CMSelectionRulesImpl} instead of this class.
   */

  /**
   * Returns the value of the document property "defaultContent"
   * @return the value of the document property "defaultContent"
   */
  @Override
  public List<CMTeasable> getDefaultContent() {
    List<Content> contents = getContent().getLinks("defaultContent");
    return createBeansFor(contents, CMTeasable.class);
  }


  /**
   * Returns the value of the document property "rules"
   * @return the value of the document property "rules"
   */
  @Override
  public Markup getRules() {
    return getContent().getMarkup("rules");
  }


  /**
   * Returns the value of the document property "master"
   * @return the value of the document property "master"
   */
  @Override
  public CMSelectionRules getMaster() {
    return (CMSelectionRules)super.getMaster();
  }

}
