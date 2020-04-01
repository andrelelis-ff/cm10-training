package com.coremedia.livecontext.ecommerce.ibm.common;

import java.util.List;

/**
 * Pojo generated from the json response from erroneous REST calls.
 */
@SuppressWarnings("unused") // values are injected by gson
class WcServiceErrors {

  private List<WcServiceError> errors;

  List<WcServiceError> getErrors() {
    return errors;
  }

}
