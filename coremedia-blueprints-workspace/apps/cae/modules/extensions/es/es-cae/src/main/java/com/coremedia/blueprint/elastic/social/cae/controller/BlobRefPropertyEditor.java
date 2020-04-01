package com.coremedia.blueprint.elastic.social.cae.controller;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

/**
 * Used to enable input of {@link BlobRef}s for Spring Webflows.
 */
public class BlobRefPropertyEditor extends PropertyEditorSupport implements PropertyEditor {

  @Override
  public void setAsText(String id) {
    setValue(new BlobRefImpl(id));
  }
}
