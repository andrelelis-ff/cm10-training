package com.coremedia.blueprint.lc.test.beans;

import com.coremedia.livecontext.ecommerce.common.CommerceBeanType;

enum TestCommerceBeanType implements CommerceBeanType {
  PRODUCTCOLOR;

  @Override
  public String type() {
    return name();
  }
}
