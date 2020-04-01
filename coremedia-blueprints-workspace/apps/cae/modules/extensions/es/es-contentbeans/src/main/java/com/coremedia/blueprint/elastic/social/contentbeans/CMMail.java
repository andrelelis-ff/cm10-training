package com.coremedia.blueprint.elastic.social.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMHasContexts;
import com.coremedia.elastic.social.api.mail.MailTemplate;

/**
 * Generated interface for beans of document type "CMMail".
 */
public interface CMMail extends CMHasContexts, MailTemplate {
  /*
   * DEVELOPER NOTE
   * Change the methods to narrow the public interface
   * of the {@link com.coremedia.blueprint.elastic.social.contentbeans.CMMailImpl} implementation bean.
   */

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMMail'
   */
  String CONTENTTYPE_CMMAIL = "CMMail";

  String getExampleText();

  String getErrorToken();
}
