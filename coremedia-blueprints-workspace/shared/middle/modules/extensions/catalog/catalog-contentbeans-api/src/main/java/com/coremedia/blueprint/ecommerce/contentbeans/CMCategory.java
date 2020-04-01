package com.coremedia.blueprint.ecommerce.contentbeans;


import com.coremedia.blueprint.ecommerce.common.contentbeans.CMAbstractCategory;
import com.coremedia.cae.aspect.Aspect;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @cm.template.api
 */
public interface CMCategory extends CMAbstractCategory {
  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMChannel'.
   */
  String NAME = "CMCategory";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a {@link CMCategory} object
   */
  @Override
  CMCategory getMaster();

  @Override
  Map<Locale, ? extends CMCategory> getVariantsByLocale();

  @Override
  Collection<? extends CMCategory> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMCategory>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMCategory>> getAspects();

  /**
   * Returns the subcategories of this category.
   *
   * @return immutable list of subcategories
   * @cm.template.api
   */
  @NonNull
  List<CMCategory> getSubcategories();

  /**
   * Returns the products of this category.
   *
   * @return immutable list of products
   * @cm.template.api
   */
  @NonNull
  List<CMProduct> getProducts();

}
