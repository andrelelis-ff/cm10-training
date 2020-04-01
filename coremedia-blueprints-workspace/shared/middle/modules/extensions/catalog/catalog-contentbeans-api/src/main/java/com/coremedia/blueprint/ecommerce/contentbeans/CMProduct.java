package com.coremedia.blueprint.ecommerce.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMDownload;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.livecontext.ecommerce.asset.CatalogPicture;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceException;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Product features of the ecommerce API are available by the {@link #getProduct}
 * delegate.  Specific features of the CMS catalog are available directly from
 * the content bean.
 * @cm.template.api
 */
public interface CMProduct extends CMTeasable {
  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMProduct'.
   */
  String NAME = "CMProduct";

  /**
   * The name of the downloads property.
   */
  String DOWNLOADS = "downloads";

  /**
   * The name of the productCode property.
   */
  String PRODUCT_CODE = "productCode";

  /**
   * The name of the productName property
   */
  String PRODUCT_NAME = "productName";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a {@link CMProduct} object
   */
  @Override
  CMProduct getMaster();

  /**
   * Returns the variants of this {@link CMProduct} indexed by their {@link java.util.Locale}
   *
   * @return the variants of this {@link CMProduct} indexed by their {@link java.util.Locale}
   */
  @Override
  Map<Locale, ? extends CMProduct> getVariantsByLocale();

  /**
   * Returns the {@link java.util.Locale} specific variants of this {@link CMProduct}
   *
   * @return the {@link java.util.Locale} specific variants of this {@link CMProduct}
   */
  @Override
  Collection<? extends CMProduct> getLocalizations();

  /**
   * Returns a <code>Map</code> from aspectIDs to Aspects. AspectIDs consists of an aspect name with a
   * prefix which identifies the plugin provider.
   *
   * @return a <code>Map</code> from aspectIDs to <code>Aspect</code>s
   *
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMProduct>> getAspectByName();

  /**
   * Returns a list of all  <code>Aspect</code>s from all availiable
   * PlugIns that are registered to this content bean.
   *
   * @return a list of {@link com.coremedia.cae.aspect.Aspect}
   *
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMProduct>> getAspects();

  /**
   * Returns the underlying Product.
   *
   * @return the product bean representing the product in the commerce system
   */
  Product getProduct();

  /**
   * Returns the product pictures.
   * @cm.template.api
   */
  @NonNull
  List<CatalogPicture> getProductPictures();

  /**
   * Returns a product picture.
   *
   * @return The first picture of {@link #getProductPictures()} or null if there is no picture.
   * @cm.template.api
   */
  CatalogPicture getProductPicture();

  /**
   * Returns the downloads attached to the product.
   *
   * @return A list of CMDownload beans
   * @cm.template.api
   */
  List<CMDownload> getDownloads();

  /**
   * Returns the product code.
   *
   * @return the product code
   * @cm.template.api
   */
  String getProductCode();

  /**
   * Returns the product name.
   *
   * @return the name of the product
   * @cm.template.api
   */
  String getProductName();
}
