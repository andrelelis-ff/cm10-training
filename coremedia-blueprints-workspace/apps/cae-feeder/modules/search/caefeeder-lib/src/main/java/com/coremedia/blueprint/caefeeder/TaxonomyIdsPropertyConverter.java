package com.coremedia.blueprint.caefeeder;

import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.cap.common.IdHelper;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * A {@link TaxonomyPropertyConverter} that returns a comma-separated string of numeric content IDs for taxonomies.
 */
public class TaxonomyIdsPropertyConverter extends TaxonomyPropertyConverter {

  @Override
  @Nullable
  protected String convertNamedTaxonomy(@NonNull NamedTaxonomy namedTaxonomy) {
    return String.valueOf(IdHelper.parseContentId(namedTaxonomy.getContent().getId()));
  }

  @Override
  @Nullable
  protected String convertTaxonomy(@NonNull CMTaxonomy taxonomy) {
    return String.valueOf(taxonomy.getContentId());
  }

}
