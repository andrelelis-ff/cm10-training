package com.coremedia.livecontext.elastic.social.cae;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

class MockResourceBundle extends ResourceBundle {
  private Collection<String> invokedForKeys = new HashSet<>();

  @Override
  protected Object handleGetObject(String key) {
    invokedForKeys.add(key);
    throw new MissingResourceException("", "", key);
  }

  @Override
  public Enumeration<String> getKeys() {
    return Collections.emptyEnumeration();
  }

  boolean invokedFor(String key) {
    return invokedForKeys.contains(key);
  }
}
