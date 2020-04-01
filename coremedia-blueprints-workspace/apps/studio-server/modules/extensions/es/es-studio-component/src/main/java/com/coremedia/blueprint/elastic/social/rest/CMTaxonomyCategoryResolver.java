package com.coremedia.blueprint.elastic.social.rest;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.elastic.social.rest.api.CategoryKeyAndDisplay;

import edu.umd.cs.findbugs.annotations.NonNull;
import javax.inject.Named;

@Named
public class CMTaxonomyCategoryResolver implements CategoryResolver {

  public static final String CMTAXONOMY_VALUE = "value";
  public static final String CMTAXONOMY_DOCTYPE = "CMTaxonomy";

  @Override
  public CategoryKeyAndDisplay resolve(@NonNull Content content) {
    if (handlesType(content.getType())) {
      String value = content.getString(CMTAXONOMY_VALUE);
      return new CategoryKeyAndDisplay(value, value);
    }

    return null;
  }

  private boolean handlesType(ContentType contentType) {
    return contentType.isSubtypeOf(CMTAXONOMY_DOCTYPE);
  }
}
