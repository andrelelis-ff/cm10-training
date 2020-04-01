package com.coremedia.livecontext.handler;

import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class LiveContextProductSeoLinkBuilderHelper {

  @Value("${livecontext.max-category-segments:2}")
  private int wcsStorefrontMaxUrlSegments = 2;

  /**
   * Return the SEO URL for the given commerce bean.
   */
  public String buildSeoSegmentsFor(@NonNull Product product) {
    StringBuilder segments = new StringBuilder();

    String seoSegment = product.getSeoSegment();
    Category category = product.getCategory();

    if (!StringUtils.isBlank(seoSegment)) {
      segments.append(buildSeoBreadCrumbs(category));
      segments.append(seoSegment);
    }

    return segments.toString();
  }

  /**
   * This method returns the string
   * with the whole category path of the current category starting with the top level category and ending with the
   * current category + '/'.
   */
  private String buildSeoBreadCrumbs(@NonNull Category category) {
    StringBuilder segments = new StringBuilder();

    List<Category> breadcrumb = category.getBreadcrumb();

    int breadcrumbSize = breadcrumb.size();
    if (breadcrumbSize > wcsStorefrontMaxUrlSegments) {
      breadcrumb = breadcrumb.subList(breadcrumbSize - wcsStorefrontMaxUrlSegments, breadcrumbSize);
    }

    for (Category c : breadcrumb) {
      segments.append(c.getSeoSegment());
      segments.append('/');
    }

    return segments.toString();
  }
}
