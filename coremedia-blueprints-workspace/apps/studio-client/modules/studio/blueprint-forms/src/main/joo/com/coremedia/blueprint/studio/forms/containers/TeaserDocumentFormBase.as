package com.coremedia.blueprint.studio.forms.containers {
import com.coremedia.cms.editor.sdk.premular.PropertyFieldGroup;

/**
 * The base class for the TeaserDocumentForm
 */
public class TeaserDocumentFormBase extends PropertyFieldGroup {
  /**
   * Create a new instance of this class.
   *
   * @param config the config object
   */
  public function TeaserDocumentFormBase(config:TeaserDocumentForm = null) {
    super(config);
  }
}
}