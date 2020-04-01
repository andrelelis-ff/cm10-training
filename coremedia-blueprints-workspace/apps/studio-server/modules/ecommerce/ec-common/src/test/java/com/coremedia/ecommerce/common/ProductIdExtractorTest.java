package com.coremedia.ecommerce.common;

import org.junit.Test;

import java.io.InputStream;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductIdExtractorTest {

  @Test
  public void testExtractInventoryInfoWithXmp() throws Exception {
    InputStream stream = getClass().getResourceAsStream("image-with-xmp-product-reference.jpg");

    Collection<String> externalIds = ProductIdExtractor.extractProductIds(stream);

    assertThat(externalIds).hasSize(2);
  }

  @Test
  public void testExtractInventoryInfoNoData() throws Exception {
    InputStream stream = getClass().getResourceAsStream("image-no-xmp.jpg");

    Collection<String> externalIds = ProductIdExtractor.extractProductIds(stream);

    assertThat(externalIds).isEmpty();
  }

  @Test
  public void testExtractInventoryInfoWrongFormat() throws Exception {
    InputStream stream = getClass().getResourceAsStream("no-pic.jpg");

    Collection<String> externalIds = ProductIdExtractor.extractProductIds(
            stream);

    assertThat(externalIds).isEmpty();
  }
}
