package com.coremedia.blueprint.cae.web.taglib;

import com.coremedia.blueprint.common.layout.Container;
import com.coremedia.blueprint.common.navigation.HasViewTypeName;
import com.coremedia.blueprint.common.util.ContainerFlattener;

import java.util.List;

/**
 * Represents custom container having elements and a viewtype name based on the given base container.
 */
class ContainerWithViewTypeName implements Container<Object>, HasViewTypeName {

  private Container baseContainer;
  private List<Object> items;

  ContainerWithViewTypeName(Container baseContainer, List<Object> items) {
    this.baseContainer = baseContainer;
    this.items = items;
  }

  Container getBaseContainer() {
    return baseContainer;
  }

  @Override
  public String getViewTypeName() {
    if (baseContainer instanceof HasViewTypeName) {
      return ((HasViewTypeName) baseContainer).getViewTypeName();
    }
    return null;
  }

  @Override
  public List<Object> getItems() {
    return items;
  }

  @Override
  public List<Object> getFlattenedItems() {
    return ContainerFlattener.flatten(this, Object.class);
  }
}
