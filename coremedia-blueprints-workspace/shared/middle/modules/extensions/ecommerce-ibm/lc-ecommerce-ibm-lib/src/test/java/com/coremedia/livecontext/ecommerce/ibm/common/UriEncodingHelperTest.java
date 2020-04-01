package com.coremedia.livecontext.ecommerce.ibm.common;

import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class UriEncodingHelperTest {

  @Test
  void testUriEncoding() {
    String uriWithSpecialCharacters = "http://shop-ref/search/resources/store/715838084/categoryview/%2520?categoryIdentifier=PC_Blouses/+%Sweaters&langId=-1";
    UriComponentsBuilder originalBuilder = UriComponentsBuilder.fromUriString(uriWithSpecialCharacters);
    UriComponents originalUriComponents = originalBuilder.build();
    UriComponents encodedUriComponents = originalUriComponents.encode();
    String encodedUri = encodedUriComponents.toUriString();

    assertThat(encodedUri).contains("+");

    String plusFixed = UriEncodingHelper.fixPlusEncoding(encodedUriComponents).toUri().toString();

    assertThat(plusFixed)
            .doesNotContain("+")
            .contains("PC_Blouses/%2B%25Sweaters");
  }
}