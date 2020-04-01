package com.coremedia.livecontext.ecommerce.ibm.login;

/**
 * Commerce credentials for a registered user.
 */
public class SimpleCommerceCredentials implements WcCredentials {

  private String  storeId;
  private WcSession session;

  public SimpleCommerceCredentials(String storeId, WcSession session) {
    this.storeId = storeId;
    this.session = session;
  }

  @Override
  public String getStoreId() {
    return storeId;
  }

  @Override
  public WcSession getSession() {
    return session;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SimpleCommerceCredentials that = (SimpleCommerceCredentials) o;

    if (session != null ? !session.equals(that.session) : that.session != null) {
      return false;
    }
    if (storeId != null ? !storeId.equals(that.storeId) : that.storeId != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = storeId != null ? storeId.hashCode() : 0;
    result = 31 * result + (session != null ? session.hashCode() : 0);
    return result;
  }
}