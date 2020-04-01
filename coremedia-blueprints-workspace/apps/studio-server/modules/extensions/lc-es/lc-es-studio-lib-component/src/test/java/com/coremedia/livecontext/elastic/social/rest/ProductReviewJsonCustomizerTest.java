package com.coremedia.livecontext.elastic.social.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.id.CommerceIdParserHelper;
import com.coremedia.elastic.social.api.comments.Comment;
import com.coremedia.elastic.social.rest.api.JsonProperties;
import com.coremedia.livecontext.commercebeans.ProductInSite;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.rest.cap.content.ContentRepositoryResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.Silent.class)
public class ProductReviewJsonCustomizerTest {

  @InjectMocks
  private ProductReviewJsonCustomizer productReviewJsonCustomizer = new ProductReviewJsonCustomizer();

  @Mock
  private Comment comment;

  @Mock
  private Product product;

  @Mock
  private ProductInSite productInSite;

  @Mock
  private ContentRepositoryResource contentRepositoryResource;

  @Mock
  private StoreContext storeContext;

  @Test
  public void customizeProduct() throws IOException, URISyntaxException {
    Map<String, Object> serializedObject = new HashMap<>();

    when(comment.getTarget()).thenReturn(productInSite);
    when(productInSite.getProduct()).thenReturn(product);
    String productName = "productName";
    when(product.getName()).thenReturn(productName);
    when(contentRepositoryResource.getPreviewControllerUrlPattern()).thenReturn("preview?id={0}");
    when(product.getId()).thenReturn(CommerceIdParserHelper.parseCommerceIdOrThrow("test:///x/product/productId"));
    when(product.getContext()).thenReturn(storeContext);
    when(storeContext.getSiteId()).thenReturn("siteId");

    productReviewJsonCustomizer.customize(comment, serializedObject);

    assertEquals(productName, serializedObject.get(JsonProperties.SUBJECT));
    assertEquals("preview?id=test:///catalog/product/productId&site=siteId", serializedObject.get(JsonProperties.PREVIEW_URL));
  }

  @Test
  public void customizeErrornousProduct() throws IOException, URISyntaxException {
    Map<String, Object> serializedObject = new HashMap<>();

    when(comment.getTarget()).thenReturn(product);
    when(product.getName()).thenThrow(NullPointerException.class);

    assertEquals(false, serializedObject.keySet().contains(JsonProperties.SUBJECT));
    assertEquals(false, serializedObject.keySet().contains(JsonProperties.PREVIEW_URL));

    productReviewJsonCustomizer.customize(comment, serializedObject);
  }

  @Test
  public void doNotCustomizeSomethingElse() throws IOException, URISyntaxException {
    Map<String, Object> serializedObject = new HashMap<>();
    when(comment.getTarget()).thenReturn(null);

    productReviewJsonCustomizer.customize(comment, serializedObject);

    assertEquals(0, serializedObject.size());
  }
}
