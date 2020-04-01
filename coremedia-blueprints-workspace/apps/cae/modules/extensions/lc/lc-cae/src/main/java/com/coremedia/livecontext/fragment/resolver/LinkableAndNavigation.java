package com.coremedia.livecontext.fragment.resolver;

import com.coremedia.cap.content.Content;

/**
 * A simple container for a content and a navigation.
 */
public class LinkableAndNavigation {
  private Content linkable;
  private Content navigation;

  public LinkableAndNavigation(Content linkable, Content navigation) {
    this.linkable = linkable;
    this.navigation = navigation;
  }

  public Content getLinkable() {
    return linkable;
  }

  public Content getNavigation() {
    return navigation;
  }
}
