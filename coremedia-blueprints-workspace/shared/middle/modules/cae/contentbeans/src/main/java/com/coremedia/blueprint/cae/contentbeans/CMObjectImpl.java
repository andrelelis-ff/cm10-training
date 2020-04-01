package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMObject;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cae.aspect.provider.AspectsProvider;
import com.coremedia.cae.aspect.provider.CompoundAspectsProvider;
import com.coremedia.cap.common.IdHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generated extension class for immutable beans of document type "CMObject".
 */
public class CMObjectImpl extends CMObjectBase {

  private CompoundAspectsProvider aspectsProviders;

  @Override
  public AspectsProvider getAspectsProvider() {
    return aspectsProviders;
  }

  public void setAspectsProviders(CompoundAspectsProvider aspectsProviders) {
    this.aspectsProviders = aspectsProviders;
  }

  @Override
  public int getContentId() {
    return IdHelper.parseContentId(getContent().getId());
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMObject>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMObject>>) aspectsProviders.getAspects(this);
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  public List<? extends Aspect<? extends CMObject>> getAspects() {
    List<Aspect<? extends CMObject>> result = new ArrayList<>();
    for (Map.Entry<String, ? extends Aspect<? extends CMObject>> entry : getAspectByName().entrySet()) {
      Aspect<? extends CMObject> value = entry.getValue();
      result.add(value);
    }
    return result;
  }

  @Override
  public String toString() {
    return getClass().getName() + "[id=" + (null == getContent() ? "<unavailable>" : getContentId()) + ']';
  }
}
