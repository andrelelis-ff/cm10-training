package com.coremedia.blueprint.themeimporter.descriptors;

import javax.xml.bind.annotation.XmlValue;

public abstract class Resource {

  private String link;

  @XmlValue
  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }
}
