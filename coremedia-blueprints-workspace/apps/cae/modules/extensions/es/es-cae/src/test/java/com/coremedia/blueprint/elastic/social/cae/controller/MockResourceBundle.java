package com.coremedia.blueprint.elastic.social.cae.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

class MockResourceBundle extends ResourceBundle {
  Collection<String> invokedForKeys = new HashSet<>();

  @Override
  protected Object handleGetObject(String key) {
    invokedForKeys.add(key);
    throw new MissingResourceException("", "", key);
  }

  @Override
  public Enumeration<String> getKeys() {
    return Collections.emptyEnumeration();
  }

  public boolean invokedFor(String key) {
    return invokedForKeys.contains(key);
  }
}
