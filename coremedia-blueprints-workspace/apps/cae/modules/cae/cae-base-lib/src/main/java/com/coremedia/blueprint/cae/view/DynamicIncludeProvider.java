package com.coremedia.blueprint.cae.view;

public interface DynamicIncludeProvider {

  public DynamicInclude getDynamicInclude(Object delegate, String view);

}
