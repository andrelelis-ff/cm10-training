package com.coremedia.blueprint.studio.topicpages.rest;

import java.util.ArrayList;
import java.util.List;

/**
 * The representation of the collected topic pages representations.
 */
public class TopicsRepresentation {
  private List<TopicRepresentation> items = new ArrayList<>();
  private boolean filtered = false;

  public List<TopicRepresentation> getItems() {
    return items;
  }

  public int size() {
    return items.size();
  }

  public boolean isFiltered() {
    return filtered;
  }

  public void setFiltered(boolean filtered) {
    this.filtered = filtered;
  }
}
