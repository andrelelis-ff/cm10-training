package com.coremedia.blueprint.personalization.forms;

import com.coremedia.blueprint.common.contentbeans.CMObject;

public class FormField {
  private CMObject bean;
  private boolean value;

  public CMObject getBean() {
    return bean;
  }

  public void setBean(CMObject bean) {
    this.bean = bean;
  }

  public boolean isValue() {
    return value;
  }

  public void setValue(boolean value) {
    this.value = value;
  }
}
