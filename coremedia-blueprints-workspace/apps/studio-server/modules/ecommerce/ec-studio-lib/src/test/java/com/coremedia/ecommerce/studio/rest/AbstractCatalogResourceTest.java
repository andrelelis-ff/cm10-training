package com.coremedia.ecommerce.studio.rest;

import org.junit.Test;

import static com.coremedia.ecommerce.studio.rest.AbstractCatalogResource.decodeId;
import static org.assertj.core.api.Assertions.assertThat;

public class AbstractCatalogResourceTest {

  /**
   * Decode a category ID encoded by the Studio frontend (ExtJS).
   */
  @Test
  public void testDecodeCategoryId() {
    String input = "ibm:///catalog/category/UK_Fish_%2B_Chips";
    String expected = "ibm:///catalog/category/UK_Fish_+_Chips";

    assertThat(decodeId(input)).isEqualTo(expected);
  }
}
