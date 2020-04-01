package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMObject;
import com.coremedia.objectserver.beans.AbstractContentBean;

/**
 * Base class for immutable beans of document type CMObject.
 * Should not be changed.
 */
public abstract class CMObjectBase extends AbstractContentBean implements CMObject {
  @Override
  public final void assumeIdentity(Object source) {
    // don't override #assumeIdentity because dataviews of beans extending AbstractContentBean will automatically get all injections
    super.assumeIdentity(source);
  }
}
  