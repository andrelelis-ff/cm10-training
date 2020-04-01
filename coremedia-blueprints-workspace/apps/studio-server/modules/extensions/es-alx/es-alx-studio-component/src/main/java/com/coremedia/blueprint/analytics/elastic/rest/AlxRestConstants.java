package com.coremedia.blueprint.analytics.elastic.rest;

public class AlxRestConstants {

  public static final String ALX_REST_SEGMENT = "/elastic/alx";

  public static final String TENANT_SEGMENT = "{tenant}";

  public static final String ALX_REST_PREFIX = TENANT_SEGMENT + ALX_REST_SEGMENT;

  private AlxRestConstants() {
    // prevent initialisation
  }
}
