package com.coremedia.livecontext.handler;

import java.io.Serializable;

/**
 * A bean containing settings for a commerce page.
 * <p>
 * The settings indicate if the commerce system have to build a part of a page on it's own or if the CMS is able to
 * render a page.
 */
@SuppressWarnings("unused") //spring needs getter to serialize.
class ManagedPagePartsSettings implements Serializable {

  private boolean isManagedHeader;
  private boolean isManagedFooter;
  private boolean isManagedNavigation;
  private boolean isManagedFooterNavigation;

  public boolean isManagedHeader() {
    return isManagedHeader;
  }

  void setManagedHeader(boolean managedHeader) {
    isManagedHeader = managedHeader;
  }

  public boolean isManagedFooter() {
    return isManagedFooter;
  }

  void setManagedFooter(boolean managedFooter) {
    isManagedFooter = managedFooter;
  }

  public boolean isManagedNavigation() {
    return isManagedNavigation;
  }

  void setManagedNavigation(boolean managedNavigation) {
    isManagedNavigation = managedNavigation;
  }

  public boolean isManagedFooterNavigation() {
    return isManagedFooterNavigation;
  }

  public void setManagedFooterNavigation(boolean managedFooterNavigation) {
    this.isManagedFooterNavigation = managedFooterNavigation;
  }
}
