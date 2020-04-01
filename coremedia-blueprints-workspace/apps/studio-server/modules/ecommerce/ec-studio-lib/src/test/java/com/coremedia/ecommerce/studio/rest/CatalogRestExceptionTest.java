package com.coremedia.ecommerce.studio.rest;

import org.junit.Test;

import static com.coremedia.ecommerce.studio.rest.CatalogRestException.DEFAULT_ERROR_CODE_PREFIX;
import static com.coremedia.ecommerce.studio.rest.CatalogRestException.getErrorName;
import static org.junit.Assert.assertEquals;

public class CatalogRestExceptionTest {

  @Test
  public void testGetErrorName() {
    assertEquals(DEFAULT_ERROR_CODE_PREFIX + "foo", getErrorName("foo"));
    assertEquals("COULD_NOT_FIND_CATALOG_BEAN", getErrorName(CatalogRestErrorCodes.COULD_NOT_FIND_CATALOG_BEAN));
    assertEquals("COULD_NOT_FIND_CATALOG", getErrorName(CatalogRestErrorCodes.COULD_NOT_FIND_CATALOG));
    assertEquals("COULD_NOT_FIND_STORE_BEAN", getErrorName(CatalogRestErrorCodes.COULD_NOT_FIND_STORE_BEAN));
  }
}
