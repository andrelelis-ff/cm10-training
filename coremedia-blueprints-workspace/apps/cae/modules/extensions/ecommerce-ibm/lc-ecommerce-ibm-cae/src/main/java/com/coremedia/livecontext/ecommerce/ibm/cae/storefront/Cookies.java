package com.coremedia.livecontext.ecommerce.ibm.cae.storefront;

import com.google.common.base.Joiner;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

class Cookies {

  private static final Logger LOG = LoggerFactory.getLogger(Cookies.class);

  private static final Joiner COOKIE_JOINER = Joiner.on("; ");

  private Cookies() {
  }

  /**
   * Add cookies from {@link HttpClientContext} to cookie header value.
   *
   * @param headerValue cookie header value. Value of the "Set-Cookie" header.
   * @param cookies     cookie names and their values
   * @return the extended cookie header value
   */
  @Nullable
  static String addCookiesToHeaderValue(@Nullable String headerValue, @NonNull Map<String, String> cookies) {
    if (cookies.isEmpty()) {
      return headerValue;
    }

    String joinedCookies = COOKIE_JOINER.withKeyValueSeparator('=').join(cookies);

    if (isNullOrEmpty(headerValue)) {
      return joinedCookies;
    } else {
      return COOKIE_JOINER.join(headerValue, joinedCookies);
    }
  }

  @Nullable
  static String decodeValue(@NonNull String encodedValue) {
    try {
      return URLDecoder.decode(encodedValue, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      String msg = "UTF-8 is not supported. This must not happen, use an approved JVM.";
      LOG.error(msg, e);
      throw new InternalError(msg);
    } catch (IllegalArgumentException iae) {
      LOG.warn("Cookie value '{}' can not be URL-decoded.", encodedValue);
      LOG.trace("Detailed exception that happened on attempted URL-decoding of cookie value:", iae);
      return null;
    }
  }
}
