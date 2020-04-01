package com.coremedia.livecontext.elastic.social.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdFormatterHelper;
import com.coremedia.common.personaldata.PersonalData;
import com.coremedia.ecommerce.studio.rest.CommerceBeanResource;
import com.coremedia.elastic.social.api.comments.Comment;
import com.coremedia.elastic.social.rest.api.JsonCustomizer;
import com.coremedia.elastic.social.rest.api.JsonProperties;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.rest.cap.content.ContentRepositoryResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

@Named
@Order(2)
public class ProductReviewJsonCustomizer implements JsonCustomizer<Comment> {

  private static final Logger LOG = LoggerFactory.getLogger(ProductReviewJsonCustomizer.class);

  @Inject
  private ContentRepositoryResource contentRepositoryResource;

  @Override
  public void customize(Comment comment, @PersonalData Map<String, Object> serializedObject) {
    if (comment.getTarget() instanceof ProductInSite) {
      Product product = ((ProductInSite) comment.getTarget()).getProduct();
      try {
        serializedObject.put(JsonProperties.SUBJECT, product.getName());
        addPreviewUrl(product, serializedObject);
      } catch (Exception exception) { // NOSONAR
        LOG.warn("An exception occurred on accessing name from Product instance", exception);
      }
    }
  }

  private void addPreviewUrl(Product product, @PersonalData Map<String, Object> serializedObject) {
    String previewControllerUriPattern = contentRepositoryResource.getPreviewControllerUrlPattern();
    String id = CommerceIdFormatterHelper.format(product.getId());
    String previewUrl = CommerceBeanResource.formatPreviewUrl(previewControllerUriPattern, id, product.getContext().getSiteId());
    serializedObject.put(JsonProperties.PREVIEW_URL, previewUrl);
  }
}
