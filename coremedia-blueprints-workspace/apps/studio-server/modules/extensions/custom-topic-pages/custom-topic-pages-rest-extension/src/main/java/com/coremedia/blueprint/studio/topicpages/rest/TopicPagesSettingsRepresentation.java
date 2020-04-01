package com.coremedia.blueprint.studio.topicpages.rest;

import com.coremedia.cap.content.Content;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation class for server side topic pages settings.
 */
public class TopicPagesSettingsRepresentation {
  private List<String> adminGroups = new ArrayList<>();
  private Content topicPagesFolder;
  private Content topicPageChannel;

  public Content getTopicPageChannel() {
    return topicPageChannel;
  }

  public void setTopicPageChannel(Content topicPageChannel) {
    this.topicPageChannel = topicPageChannel;
  }

  public Content getFolder() {
    return topicPagesFolder;
  }

  public void setFolder(Content topicPageFolder) {
    this.topicPagesFolder = topicPageFolder;
  }

  public List<String> getAdminGroups() {
    return adminGroups;
  }
}
