package com.coremedia.livecontext.fragment;

/**
 * The <code>FragmentContext</code> is used to store arbitrary information needed while
 * rendering a fragment. It will be created and provided by the {@link FragmentContextProvider}
 */
public class FragmentContext {
  private FragmentParameters parameters;
  private boolean fragmentRequest = false;

  /**
   * Returns whether the current request is a fragment request or not (provided {@link #setFragmentRequest} was
   * called accordingly. The default value is <code>false</code>.
   *
   * @return <code>true</code> if the current request is a fragment request
   */
  public boolean isFragmentRequest() {
    return fragmentRequest;
  }

  /**
   * Must be called by any fragment handler to let everyone know that it is executed within the context
   * of a fragment request coming from the commerce system. This is used for example for generating
   * absolute URLs which is necessary for fragments that are embedded within a commerce web page. Relative
   * URLs would be handled by the commerce system not as intended by the CAE.
   *
   * @param fragmentRequest <code>true</code> if the current request is a fragment request.
   */
  public void setFragmentRequest(boolean fragmentRequest) {
    this.fragmentRequest = fragmentRequest;
  }

  /**
   * Returns the FragmentParameters that have been passed for this fragment request.
   *
   * @return parameters created by the FragmentContextProvider together with this context.
   */
  public FragmentParameters getParameters() {
    return parameters;
  }

  /**
   * Sets the fragment parameters of the request to this context so that they are accessible by all other filters
   * and interceptors.
   * @param parameters the FragmentParameters created the the {@see FragmentParametersFactory}.
   */
  public void setParameters(FragmentParameters parameters) {
    this.parameters = parameters;
  }
}
