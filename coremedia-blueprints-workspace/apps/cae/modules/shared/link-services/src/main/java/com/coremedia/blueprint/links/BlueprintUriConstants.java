package com.coremedia.blueprint.links;

import com.coremedia.blueprint.base.links.UriConstants;

/**
 * This class contains constants that are used in Handlers and Linkschemes.
 */
public final class BlueprintUriConstants {

  /**
   * Use these constants as prefixes for URI Patterns.
   * <p>
   * Apache and Varnish rewrite rules are provided.
   * <p>
   * These prefixes do not apply to ordinary content URLs.  They are not
   * SEO relevant, and users will never see them in their browsers' address
   * fields.  The values have counterparts in non-Java code (e.g. rewrite
   * rules) and thus must not be changed carelessly.
   */
  public static final class Prefixes {
    /**
     * Prefix for links to resources like images, CSS or JavaScript
     */
    public static final String PREFIX_RESOURCE = "resource";
    /**
     * Prefix for links to services like RSS Feeds or SiteMap
     */
    public static final String PREFIX_SERVICE = "service";
    /**
     * Prefix for expensive internal operations, to be blocked by Apache & Co (e.g. sitemap generation).
     */
    public static final String PREFIX_INTERNAL = "internal";

    private Prefixes() {
      //hide utility class constructor
    }
  }

  public static final class Patterns {
    /**
     * This pattern is used for Interceptors handling requests that should never be cached.
     */
    public static final String DYNAMIC_URI_PATTERN = '/' + UriConstants.Segments.PREFIX_DYNAMIC + "/{all:"+ UriConstants.Patterns.PATTERN_SEGMENTS +"}";

    // static class
    private Patterns() {}
  }

  // static class
  private BlueprintUriConstants() {}
}
