package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMFolderProperties;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.content.Content;

import java.util.List;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMFolderProperties.
 * Should not be changed.
 */
public abstract class CMFolderPropertiesBase extends CMLocalizedImpl implements CMFolderProperties {

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings({"unchecked"})
  public Map<String, ? extends Aspect<? extends CMFolderProperties>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMFolderProperties>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings({"unchecked"})
  public List<? extends Aspect<? extends CMFolderProperties>> getAspects() {
    return (List<? extends Aspect<? extends CMFolderProperties>>) super.getAspects();
  }

  @Override
  public List<? extends CMContext> getContexts() {
    List<Content> contents = getContent().getLinks(CONTEXTS);
    return createBeansFor(contents, CMContext.class);
  }
}
