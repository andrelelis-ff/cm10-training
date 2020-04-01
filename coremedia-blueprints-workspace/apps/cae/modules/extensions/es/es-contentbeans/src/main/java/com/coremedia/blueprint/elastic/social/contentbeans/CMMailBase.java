package com.coremedia.blueprint.elastic.social.contentbeans;

import com.coremedia.blueprint.cae.contentbeans.CMHasContextsImpl;
import com.coremedia.cap.common.CapBlobRef;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Generated base class for beans of document type "CMMail".
 */
public abstract class CMMailBase extends CMHasContextsImpl {
  /*
   * DEVELOPER NOTE
   * Change {@link com.coremedia.blueprint.elastic.social.contentbeans.CMMailImpl} instead of this class.
   */

  /**
   * Returns the value of the document property "text"
   *
   * @return the value of the document property "text"
   */
  public String getText() {
    try {
      CapBlobRef text = getContent().getBlobRef("text");
      if (text == null) {
        return "";
      }

      try (InputStream inputStream = text.getInputStream()) {
        return IOUtils.toString(inputStream, "UTF-8");
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("Couldn't write blob 'text' to message template", e);
    }
  }

  /**
   * Returns the value of the document property "subject"
   *
   * @return the value of the document property "subject"
   */
  public String getSubject() {
    return getContent().getString("subject");
  }

  /**
   * Returns the value of the document property "from"
   *
   * @return the value of the document property "from"
   */
  public String getFrom() {
    return getContent().getString("from");
  }
}
