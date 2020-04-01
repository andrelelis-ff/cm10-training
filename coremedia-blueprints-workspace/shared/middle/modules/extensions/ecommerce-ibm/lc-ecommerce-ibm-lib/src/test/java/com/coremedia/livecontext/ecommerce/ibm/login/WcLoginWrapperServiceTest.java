package com.coremedia.livecontext.ecommerce.ibm.login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WcLoginWrapperServiceTest {

  private WcLoginWrapperService testling;

  @BeforeEach
  void setup() {
    testling = new WcLoginWrapperService();
  }

  @Test
  void testEqualsWithTypeConversion() {
    assertTrue(testling.equalsWithTypeConversion("3", 3.5));
    assertTrue(testling.equalsWithTypeConversion("3", 3));
  }

  @Test
  void testEqualsWithTypeConversionNFException() {
    assertThrows(NumberFormatException.class, () -> testling.equalsWithTypeConversion("3.5", 3.5));
  }
}