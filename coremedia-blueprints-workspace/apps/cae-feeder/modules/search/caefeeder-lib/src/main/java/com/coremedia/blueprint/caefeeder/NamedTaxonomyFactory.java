package com.coremedia.blueprint.caefeeder;

import com.coremedia.cap.content.Content;

import java.util.function.Function;

/**
 * Function that returns a new {@link NamedTaxonomy} for a taxonomy content.
 */
public class NamedTaxonomyFactory implements Function<Content, NamedTaxonomy> {
  @Override
  public NamedTaxonomy apply(Content input) {
    return new NamedTaxonomy(input);
  }
}
