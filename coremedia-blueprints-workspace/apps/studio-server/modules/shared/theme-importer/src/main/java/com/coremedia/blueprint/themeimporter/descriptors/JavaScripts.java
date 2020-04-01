package com.coremedia.blueprint.themeimporter.descriptors;


import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JavaScripts {
  @XmlElement
  private List<JavaScript> javaScript = new ArrayList<>();

  public List<JavaScript> getJavaScripts() {
    return javaScript!=null ? javaScript : Collections.emptyList();
  }

  public void setJavaScript(List<JavaScript> javaScript) {
    this.javaScript = javaScript;
  }
}