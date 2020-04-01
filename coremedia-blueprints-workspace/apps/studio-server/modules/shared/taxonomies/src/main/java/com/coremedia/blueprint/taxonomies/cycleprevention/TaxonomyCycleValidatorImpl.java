package com.coremedia.blueprint.taxonomies.cycleprevention;

import com.coremedia.blueprint.taxonomies.TaxonomyUtil;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

class TaxonomyCycleValidatorImpl implements TaxonomyCycleValidator {
  static final String CHILDREN_ATTRIBUTE_IDENTIFIER = "children";

  @Override
  public boolean isCyclic(@NonNull Content tax, ContentType contentType) {
    return checkCycle(tax, new ArrayList<>(), contentType);
  }

  private boolean checkCycle(@NonNull Content tax, @NonNull List<String> path, ContentType contentType) {
    if (!isTaxonomy(tax, contentType)) {
      return false;
    }

    String taxonomyId = tax.getId();

    if (path.contains(taxonomyId)) {
      return true;
    }

    path.add(taxonomyId);
    List<Content> children = tax.getLinks(CHILDREN_ATTRIBUTE_IDENTIFIER);
    if (children.isEmpty()) {
      return false;
    }

    if (tax.isDestroyed() || !tax.isInProduction()) {
      return false;
    }

    for (Content child : children) {
      if (checkCycle(child, path, contentType)) {
        return true;
      }
    }

    return false;
  }

  @VisibleForTesting
  boolean isTaxonomy(@NonNull Content tax, ContentType contentType) {
    return TaxonomyUtil.isTaxonomy(tax, contentType);
  }
}
