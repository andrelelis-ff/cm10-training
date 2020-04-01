package com.coremedia.blueprint.cae.web.i18n;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

class EmptyResourceBundle extends ResourceBundle {
  private static final EmptyResourceBundle instance = new EmptyResourceBundle();

  private EmptyResourceBundle() {}

  public static ResourceBundle emptyResourceBundle() {
    return instance;
  }

  @Override
  protected Object handleGetObject(String key) {
    return null;
  }

  @Override
  public Enumeration<String> getKeys() {
    return new Enumeration<String>() {
      @Override
      public boolean hasMoreElements() {
        return false;
      }

      @Override
      public String nextElement() {
        throw new NoSuchElementException("Check hasMoreElements, and you would have known.");
      }
    };
  }
}
