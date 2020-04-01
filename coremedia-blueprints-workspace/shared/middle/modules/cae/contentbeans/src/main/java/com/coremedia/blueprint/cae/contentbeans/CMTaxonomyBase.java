package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.content.Content;

import java.util.List;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMTaxonomy.
 * Should not be changed.
 */
public abstract class CMTaxonomyBase extends CMTeasableImpl implements CMTaxonomy {
  /**
   * Returns the value an aspect by name}.
   *
   * @return a list of {@link CMTaxonomy} object
   *
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings({"unchecked"})
  public Map<String, ? extends Aspect<? extends CMTaxonomy>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMTaxonomy>>) super.getAspectByName();
  }

  /**
   * Returns all aspects.
   *
   * @return a list of {@link CMTaxonomy} object
   *
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings({"unchecked"})
  public List<? extends Aspect<? extends CMTaxonomy>> getAspects() {
    return (List<? extends Aspect<? extends CMTaxonomy>>) super.getAspects();
  }

  /**
   * Returns the value of the document property {@link #VALUE}.
   *
   * @return the value of the document property {@link #VALUE}
   */
  @Override
  public String getValue() {
    return getContent().getString(VALUE);
  }

  /**
   * Returns the value of the document property {@link #CHILDREN}.
   *
   * @return the value of the document property {@link #CHILDREN}
   */
  @Override
  public List<? extends CMTaxonomy> getChildren() {
    List<Content> contents = getContent().getLinksFulfilling(CHILDREN, IS_IN_PRODUCTION);
    return createBeansFor(contents, CMTaxonomy.class);
  }

  /**
   * Returns the value of the document property {@link #EXTERNAL_REFERENCE}.
   *
   * @return the value of the document property {@link #EXTERNAL_REFERENCE}
   */
  @Override
  public String getExternalReference() {
    return getContent().getString(EXTERNAL_REFERENCE);
  }
}
