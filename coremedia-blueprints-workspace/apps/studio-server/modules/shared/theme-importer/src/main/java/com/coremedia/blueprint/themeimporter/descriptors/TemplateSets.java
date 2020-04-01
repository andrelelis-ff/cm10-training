package com.coremedia.blueprint.themeimporter.descriptors;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class TemplateSets {

  private List<TemplateSet> templateSet;

  @XmlElement
  public List<TemplateSet> getTemplateSet() {
    return this.templateSet;
  }

  public void setTemplateSet(List<TemplateSet> templateSets) {
    this.templateSet = templateSets;
  }
}
