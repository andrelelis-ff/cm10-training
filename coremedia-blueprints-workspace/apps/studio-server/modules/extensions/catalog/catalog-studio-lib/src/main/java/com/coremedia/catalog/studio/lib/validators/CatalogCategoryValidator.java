package com.coremedia.catalog.studio.lib.validators;

import com.coremedia.cap.content.Content;
import com.coremedia.rest.cap.validation.ContentTypeValidatorBase;
import com.coremedia.rest.validation.Issues;
import com.coremedia.rest.validation.Severity;

import java.util.Set;

public class CatalogCategoryValidator extends ContentTypeValidatorBase {
  private static final String CODE_ISSUE_NOT_IN_CATALOG = "categoryIsNotLinkedInCatalog";
  private static final String SETTINGS_TYPE = "CMSettings";
  public static final String CHILDREN_PROPERTY_NAME = "children";

  private String liveContextSettingName;

  public void setLiveContextSettingName(String liveContextSettingName) {
    this.liveContextSettingName = liveContextSettingName;
  }


  @Override
  public void validate(Content content, Issues issues) {
    Set<Content> parentCategories = content.getReferrersWithDescriptor(getContentType(), CHILDREN_PROPERTY_NAME);
    if(parentCategories.isEmpty() && !isRootCategory(content)){
      issues.addIssue(Severity.WARN, null, CODE_ISSUE_NOT_IN_CATALOG);
    }
  }

  private boolean isRootCategory(Content content) {
    Set<Content> settings = content.getReferrersWithType(SETTINGS_TYPE);
    for (Content setting : settings) {
      if(setting.getName().equals(liveContextSettingName)) {
        return true;
      }
    }
    return false;
  }
}
