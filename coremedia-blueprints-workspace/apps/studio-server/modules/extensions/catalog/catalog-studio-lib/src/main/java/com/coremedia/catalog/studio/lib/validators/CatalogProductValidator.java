package com.coremedia.catalog.studio.lib.validators;

import com.coremedia.cap.content.Content;
import com.coremedia.rest.cap.validation.ContentTypeValidator;
import com.coremedia.rest.validation.Issues;
import com.coremedia.rest.validation.Severity;
import com.google.common.annotations.VisibleForTesting;

import java.util.List;

public class CatalogProductValidator extends ContentTypeValidator {
  @VisibleForTesting static final String CODE_ISSUE_NOT_IN_CATALOG = "productIsNotLinkedInCatalog";
  @VisibleForTesting static final String CONTEXTS_PROPERTY_NAME = "contexts";

  @Override
  public void validate(Content content, Issues issues) {
    super.validate(content, issues);
    List<Content> parentCategories = content.getLinks(CONTEXTS_PROPERTY_NAME);
    if(parentCategories.isEmpty()){
      issues.addIssue(Severity.ERROR, CONTEXTS_PROPERTY_NAME, CODE_ISSUE_NOT_IN_CATALOG);
    }
  }
}
