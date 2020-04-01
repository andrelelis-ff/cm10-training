package com.coremedia.blueprint.caefeeder;

import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * A {@link TaxonomyPropertyConverter} that returns a comma-separated string of taxonomy names.
 */
public class TaxonomyNamesPropertyConverter extends TaxonomyPropertyConverter {

  @Override
  @Nullable
  protected String convertNamedTaxonomy(@NonNull NamedTaxonomy namedTaxonomy) {
    return namedTaxonomy.getName();
  }

  @Override
  @Nullable
  protected String convertTaxonomy(@NonNull CMTaxonomy taxonomy) {
    return taxonomy.getValue();
  }

}
