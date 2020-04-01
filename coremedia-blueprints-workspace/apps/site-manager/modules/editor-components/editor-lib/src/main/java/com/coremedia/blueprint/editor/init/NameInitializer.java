package com.coremedia.blueprint.editor.init;

import hox.corem.editor.initialization.Initializer2;
import hox.corem.editor.proxy.DocumentModel;
import hox.corem.editor.proxy.PropertyTypeModel;

public class NameInitializer implements Initializer2 {

  @Override
  public Object getInitialValue(DocumentModel document, PropertyTypeModel propertyType) {
    return document.getName();
  }
}