package com.coremedia.blueprint.themeimporter.descriptors;

import edu.umd.cs.findbugs.annotations.NonNull;

import javax.xml.bind.annotation.XmlElement;
import java.util.Collections;
import java.util.List;

public class ResourceBundles {

  @XmlElement
  private List<ResourceBundle> resourceBundle;

  @NonNull
  public List<ResourceBundle> getResourceBundles() {
    return resourceBundle != null ? resourceBundle : Collections.emptyList();
  }


  public void setResourceBundles(List<ResourceBundle> resourceBundle) {
    this.resourceBundle = resourceBundle;
  }
}
