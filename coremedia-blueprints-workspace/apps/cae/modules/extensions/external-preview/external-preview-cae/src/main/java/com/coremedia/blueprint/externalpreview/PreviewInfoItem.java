package com.coremedia.blueprint.externalpreview;

import java.util.Date;

/**
 * Representation of an preview for a user.
 */
public class PreviewInfoItem {
  private String token;
  private String data;
  private Date lastUpdate = new Date();

  /**
   * Creates a new preview information, identified by the given token.
   * @param token The user token.
   * @param data The preview data.
   */
  public PreviewInfoItem(String token, String data) {
    this.token = token;
    this.data = data;
  }

  /**
   * Returns the token that identifies this item.
   * @return token
   */
  public String getToken() {
    return token;
  }

  /**
   * The preview data is already json, so this works like a regular getter.
   * @return data as json
   */
  public String asJSON() {
    return data;
  }

  /**
   * Returns true if the preview data is still valid.
   * @return True if this info item is valid.
   */
  public boolean isValid() {
    long time = new Date().getTime();
    return (time-lastUpdate.getTime())/1000 < 3600; //NOSONAR //valid for 1 hour
  }

  @Override
  public String toString() {
    return "Preview Token '" + token + "' (" + data + ")";
  }
}
