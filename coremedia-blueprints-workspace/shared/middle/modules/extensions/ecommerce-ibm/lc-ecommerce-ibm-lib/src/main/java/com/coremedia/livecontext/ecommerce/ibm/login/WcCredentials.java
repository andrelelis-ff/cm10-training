package com.coremedia.livecontext.ecommerce.ibm.login;

/**
 * Provides information to make authenticated REST API calls.
 */
public interface WcCredentials {
  String getStoreId();
  WcSession getSession();
}