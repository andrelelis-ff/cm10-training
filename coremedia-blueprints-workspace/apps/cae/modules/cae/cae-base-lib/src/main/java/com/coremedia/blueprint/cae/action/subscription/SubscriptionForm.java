package com.coremedia.blueprint.cae.action.subscription;

/**
 * Bean representation for a newsletter subscription
 */
public class SubscriptionForm {

  private String email;
  private boolean subscribed = false;

  /**
   * @return true, if the current user is already subscribed
   */
  public boolean isSubscribed() {
    return subscribed;
  }

  public void setSubscribed(boolean subscribed) {
    this.subscribed = subscribed;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * @return The subscription email address
   */
  public String getEmail() {
    return email;
  }

}
