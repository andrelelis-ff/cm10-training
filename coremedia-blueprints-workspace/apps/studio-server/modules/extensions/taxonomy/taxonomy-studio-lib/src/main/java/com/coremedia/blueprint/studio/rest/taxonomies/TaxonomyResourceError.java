package com.coremedia.blueprint.studio.rest.taxonomies;

import org.springframework.http.HttpStatus;

/**
 * Error Codes for {@link TaxonomyResourceException}.
 */
enum TaxonomyResourceError {
  STRATEGY_NOT_FOUND(TaxonomyResourceErrorGroup.TAXONOMY_STRATEGY, HttpStatus.NOT_FOUND, "Taxonomy Strategy Not Found");

  private final String errorCode;
  private final HttpStatus httpStatus;
  private final String errorName;

  TaxonomyResourceError(TaxonomyResourceErrorGroup errorCode, HttpStatus httpStatus, String errorName) {
    this.errorCode = errorCode.format(httpStatus);
    this.httpStatus = httpStatus;
    this.errorName = errorName;
  }

  String getErrorCode() {
    return errorCode;
  }

  HttpStatus getHttpStatus() {
    return httpStatus;
  }

  String getErrorName() {
    return errorName;
  }

  private enum TaxonomyResourceErrorGroup {
    TAXONOMY_STRATEGY(1);

    private final int groupId;

    TaxonomyResourceErrorGroup(int groupId) {
      this.groupId = groupId;
    }

    String format(HttpStatus status) {
      return String.format("TAX-%02d%d", groupId, status.value());
    }
  }
}
