package com.coremedia.livecontext.handler;

import com.coremedia.livecontext.contentbeans.CMExternalPage;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

/**
 * LC Url Provider to help with formatted not encoded commerce URLs.
 */
@DefaultAnnotation(NonNull.class)
public interface LiveContextUrlProvider {

  /**
   * Build a link for the given category in the commerce system.
   *
   * @param category    the category
   * @param queryParams additional link parameters
   * @param request     the current request
   * @return a URI components builder for the link to the commerce system
   */
  Optional<UriComponentsBuilder> buildCategoryLink(Category category,
                                                   Map<String, Object> queryParams,
                                                   HttpServletRequest request);

  /**
   * Build a link for the given product in the commerce system.
   *
   * @param product     the product
   * @param queryParams additional link parameters
   * @param request     the current request
   * @return a URI components builder for the link to the commerce system
   */
  Optional<UriComponentsBuilder> buildProductLink(Product product,
                                                  Map<String, Object> queryParams,
                                                  HttpServletRequest request);

  /**
   * Build a link for the given external navigation page in the commerce system.
   *
   * @param navigation  the exernal page document
   * @param queryParams additional link parameters
   * @param request     the current request
   * @return a URI components builder for the link to the commerce system
   */
  Optional<UriComponentsBuilder> buildPageLink(CMExternalPage navigation,
                                               Map<String, Object> queryParams,
                                               HttpServletRequest request,
                                               StoreContext storeContext);

  /**
   * Build a SEO link for the commerce system.
   *
   * @param seoSegments the SEO segments String
   * @param queryParams additional link parameters
   * @param request     the current request
   * @return a URI components builder for the link to the commerce system
   */
  Optional<UriComponentsBuilder> buildShopLink(String seoSegments,
                                               Map<String, Object> queryParams,
                                               HttpServletRequest request,
                                               StoreContext storeContext);
}
