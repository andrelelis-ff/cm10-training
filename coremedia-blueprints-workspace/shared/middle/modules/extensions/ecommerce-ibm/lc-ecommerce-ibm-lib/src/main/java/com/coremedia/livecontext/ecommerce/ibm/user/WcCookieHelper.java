package com.coremedia.livecontext.ecommerce.ibm.user;

/**
 * Duplicate all IBM Preview Cookies returned by the shop preview to default cookies
 * since some rest calls support ibm preview cookies and others support default cookies.
 */
public class WcCookieHelper {

  private static final String COOKIE_PREFIX_PREVIEW = "WCP_";
  private static final String COOKIE_PREFIX_DEFAULT = "WC_";

  public static String rewritePreviewCookies(String cookieHeader) {
    if (cookieHeader != null && cookieHeader.contains(COOKIE_PREFIX_PREVIEW)) {
      StringBuilder sb = new StringBuilder();
      sb.append(cookieHeader);
      String[] cookies = cookieHeader.split(";");
      for (String cookie : cookies) {
        if (cookie.trim().startsWith(COOKIE_PREFIX_PREVIEW)) {
          String duplicateCookie = cookie.replaceFirst(COOKIE_PREFIX_PREVIEW, COOKIE_PREFIX_DEFAULT);
          sb.append(" ;").append(duplicateCookie);
        }
      }
      return sb.toString();
    }
    return cookieHeader;
  }
}
