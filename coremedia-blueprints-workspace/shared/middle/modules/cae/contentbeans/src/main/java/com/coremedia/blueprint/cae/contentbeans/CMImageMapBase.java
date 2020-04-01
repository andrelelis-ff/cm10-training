package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMImageMap;

public abstract class CMImageMapBase extends CMTeaserImpl implements CMImageMap {

  @Override
  public CMImageMap getMaster() {
    return (CMImageMap) super.getMaster();
  }
}
