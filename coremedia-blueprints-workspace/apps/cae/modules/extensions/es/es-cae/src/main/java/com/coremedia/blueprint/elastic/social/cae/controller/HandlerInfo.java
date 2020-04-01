package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.cap.multisite.SitesService;
import com.coremedia.elastic.core.api.models.Model;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class HandlerInfo {
  
  private boolean success = true;
  private String link;
  private Model model;
  private Collection<String> errors;
  // new way to add info and errors
  private List<Message> messages = new ArrayList<>();

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public Collection<String> getErrors() {
    return errors == null ? Collections.<String>emptyList() : errors;
  }

  public void setErrors(Collection<String> errors) {
    this.errors = errors;
  }

  public void addMessage(@NonNull String type, String path, @NonNull String text) {
    messages.add(new Message(path, text, type));
  }

  public Model getModel() {
    return model;
  }

  public void setModel(Model model) {
    this.model = model;
  }

  public List<Message> getMessages() {
    return messages == null ? Collections.<Message>emptyList() : messages;
  }

  public static class Message {
    private String path;
    private String text;
    private String type;

    public Message(String path, String text, String type) {
      this.path = path;
      this.text = text;
      this.type = type;
    }

    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }

    public String getText() {
      return text;
    }

    public void setText(String text) {
      this.text = text;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }
  }
}
