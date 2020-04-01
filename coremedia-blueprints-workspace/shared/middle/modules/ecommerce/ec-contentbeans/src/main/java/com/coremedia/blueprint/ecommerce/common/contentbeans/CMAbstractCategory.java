package com.coremedia.blueprint.ecommerce.common.contentbeans;


import com.coremedia.blueprint.common.contentbeans.CMChannel;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.struct.Struct;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @cm.template.api
 */
public interface CMAbstractCategory extends CMChannel {
  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMAbstractCategory'.
   */
  String NAME = "CMAbstractCategory";

  /**
   * The name of the pdp pagegrid property
   */
  String PDP_PAGEGRID = "pdpPagegrid";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a {@link CMAbstractCategory} object
   */
  @Override
  CMAbstractCategory getMaster();

  @Override
  Map<Locale, ? extends CMAbstractCategory> getVariantsByLocale();

  @Override
  Collection<? extends CMAbstractCategory> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMAbstractCategory>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMAbstractCategory>> getAspects();

  /**
   * Returns the value of the document property {@link #PDP_PAGEGRID}.
   *
   * @return the value of the document property {@link #PDP_PAGEGRID}
   */
  Struct getPdpPagegridStruct();

}
