package com.coremedia.blueprint.personalization.elastic;

import org.springframework.beans.factory.annotation.Required;

public class InterestsConfiguration {

  private String implicitSubjectTaxonomyContextName;
  private String implicitLocationTaxonomyContextName;
  private String explicitContextName;

  @Required
  public void setExplicitContextName(String explicitContextName) {
    this.explicitContextName = explicitContextName;
  }

  @Required
  public void setImplicitSubjectTaxonomyContextName(String implicitSubjectTaxonomyContextName) {
    this.implicitSubjectTaxonomyContextName = implicitSubjectTaxonomyContextName;
  }

  @Required
  public void setImplicitLocationTaxonomyContextName(String implicitLocationTaxonomyContextName) {
    this.implicitLocationTaxonomyContextName = implicitLocationTaxonomyContextName;
  }

  public String getImplicitSubjectTaxonomyContextName() {
    return implicitSubjectTaxonomyContextName;
  }

  public String getImplicitLocationTaxonomyContextName() {
    return implicitLocationTaxonomyContextName;
  }

  public String getExplicitContextName() {
    return explicitContextName;
  }

  @Override
  public String toString() {
    return "InterestsConfiguration{" +
            "implicitSubjectTaxonomyContextName='" + implicitSubjectTaxonomyContextName + '\'' +
            ", implicitLocationTaxonomyContextName='" + implicitLocationTaxonomyContextName + '\'' +
            ", explicitContextName='" + explicitContextName + '\'' +
            '}';
  }
}
