package com.coremedia.blueprint.studio.rest.taxonomies;

import com.coremedia.rest.cap.exception.ParameterizedException;

/**
 * Signals failures in {@link TaxonomyResource}.
 */
public class TaxonomyResourceException extends ParameterizedException {
  private static final long serialVersionUID = 8721385056567227884L;

  public TaxonomyResourceException(TaxonomyResourceError taxonomyResourceError,
                                   String message) {
    super(taxonomyResourceError.getHttpStatus(), taxonomyResourceError.getErrorCode(), taxonomyResourceError.getErrorName(), message);
  }
}
