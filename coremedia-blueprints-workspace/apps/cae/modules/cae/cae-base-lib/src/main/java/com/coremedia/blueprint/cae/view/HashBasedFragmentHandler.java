package com.coremedia.blueprint.cae.view;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * A wrapper class for bean includes
 *
 * @cm.template.api
 */
public class HashBasedFragmentHandler extends DynamicInclude {

  public static final String MODIFIED_PARAMETERS_HEADER_PREFIX = "CM_MODIFIED_PARAMETERS_";

  private List<String> validParameters;

  public HashBasedFragmentHandler(Object delegate, String view, List<String> validParameters) {
    super(delegate, view);
    this.validParameters = ImmutableList.copyOf(validParameters);
  }

  /**
   * @cm.template.api
   */
  public List<String> getValidParameters() {
    return validParameters;
  }

  /**
   * @cm.template.api
   */
  public String getModifiedParametersHeaderPrefix() {
    return MODIFIED_PARAMETERS_HEADER_PREFIX;
  }
}
