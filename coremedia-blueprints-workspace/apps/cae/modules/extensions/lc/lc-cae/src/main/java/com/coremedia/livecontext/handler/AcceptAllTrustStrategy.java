package com.coremedia.livecontext.handler;

import org.apache.http.conn.ssl.TrustStrategy;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * ATTENTION: For DEMO use only
 */
public class AcceptAllTrustStrategy implements TrustStrategy {

  @Override
  public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    return true;
  }
}
