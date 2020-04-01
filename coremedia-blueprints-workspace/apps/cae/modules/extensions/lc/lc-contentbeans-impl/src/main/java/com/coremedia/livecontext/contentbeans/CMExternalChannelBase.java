package com.coremedia.livecontext.contentbeans;

import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.blueprint.ecommerce.common.contentbeans.CMAbstractCategoryImpl;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.common.NoSuchPropertyDescriptorException;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.struct.Struct;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class CMExternalChannelBase extends CMAbstractCategoryImpl implements CMExternalChannel {

  // --- Standard Blueprint typing overrides ------------------------

  @Override
  public CMExternalChannel getMaster() {
    return (CMExternalChannel) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMExternalChannel> getVariantsByLocale() {
    return getVariantsByLocale(CMExternalChannel.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMExternalChannel> getLocalizations() {
    return (Collection<? extends CMExternalChannel>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMExternalChannel>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMExternalChannel>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMExternalChannel>> getAspects() {
    return (List<? extends Aspect<? extends CMExternalChannel>>) super.getAspects();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<String> getCommerceChildrenIds() {
    Map<String, Object> commerceStruct = getCommerceStructAsMap();
    List<String> children = (List<String>) commerceStruct.get(CMExternalChannel.COMMERCE_CHILDREN);
    return children != null ? children : Collections.<String>emptyList();
  }

  @Override
  public boolean isCommerceChildrenSelected() {
    Map<String, Object> commerceStruct = getCommerceStructAsMap();
    return commerceStruct != null && Boolean.valueOf(commerceStruct.get(CMExternalChannel.COMMERCE_SELECT_CHILDREN) + "");
  }

  public Map<String, Object> getCommerceStructAsMap() {
    Struct commerceStruct = null;
    Struct localSettings = getLocalAndLinkedSettings();
    if (localSettings != null) {
      try {
        commerceStruct = localSettings.getStruct(CMExternalChannel.COMMERCE_STRUCT);
      } catch (NoSuchPropertyDescriptorException ex) {
        // do nothing
      }
    }
    return commerceStruct != null ? commerceStruct.toNestedMaps() : Collections.<String, Object>emptyMap();
  }

  // --- Features ---------------------------------------------------

  @Override
  public List<? extends Linkable> getChildren() {
    List<? extends Linkable> internalChildren = super.getChildren();
    List<Linkable> externalChildren = getExternalChildren(getSitesService().getContentSiteAspect(getContent()).getSite());
    return merge(internalChildren, externalChildren);
  }

  protected abstract List<Linkable> getExternalChildren(Site siteId);

  private List<? extends Linkable> merge(List<? extends Linkable> internalChildren, List<Linkable> externalChildren) {
    return externalChildren != null ? externalChildren : Collections.<Navigation>emptyList();
  }

}
