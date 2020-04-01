package com.coremedia.livecontext.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.struct.Struct;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @cm.template.api
 */
public interface CMExternalProduct extends CMTeasable {

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMExternalProduct'.
   */
  String NAME = "CMExternalProduct";

  /**
   * Name of the document property 'externalId'.
   * <p>
   * <p>Useful for queries and content level code.
   */
  String EXTERNAL_ID = "externalId";

  /**
   * The name of the pagegrid property
   */
  String PAGEGRID = "pdpPagegrid";

  @Override
  CMExternalProduct getMaster();

  @Override
  Map<Locale, ? extends CMExternalProduct> getVariantsByLocale();

  @Override
  Collection<? extends CMExternalProduct> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMExternalProduct>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMExternalProduct>> getAspects();

  /**
   * <p>
   * Returns the content property {@link #EXTERNAL_ID}. The returned value is the full commerce id including
   * prefix for vendor and bean type.
   * </p>
   * Note that {@link CommerceBean#getExternalId()} is the unprefixed ID format.
   *
   * @return the formatted commerce id
   */
  String getExternalId();

  /**
   * Returns the value of the document property {@link #PAGEGRID}.
   *
   * @return the value of the document property {@link #PAGEGRID}
   */
  Struct getPagegridStruct();
}
