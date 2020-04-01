package com.coremedia.livecontext.ecommerce.ibm.cae.storefront;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

class CookiesTest {

  @Test
  void addCookiesToHeaderValue() {
    String headerValue
            = "JSESSIONID=0000Ud0wt1euajipq9m1v4bpd6D:-1; HTTPOnly; Path=/; Domain=.toko-16-01.coremedia.vm; HttpOnly";

    Map<String, String> cookies = singletonMap("k1", "v1");
    String actual;

    actual = Cookies.addCookiesToHeaderValue(headerValue, cookies);
    assertThat(actual).endsWith("; k1=v1");

    actual = Cookies.addCookiesToHeaderValue(null, cookies);
    assertThat(actual).isEqualTo("k1=v1");

    actual = Cookies.addCookiesToHeaderValue("", cookies);
    assertThat(actual).isEqualTo("k1=v1");
  }
}
