package com.coremedia.blueprint.elastic.social.cae.view;

import com.coremedia.blueprint.cae.view.AbstractJsonView;
import com.coremedia.blueprint.elastic.social.cae.controller.HandlerInfo;
import com.coremedia.elastic.core.api.models.Model;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Renders a JSON representation of a {@link com.coremedia.blueprint.elastic.social.cae.controller.HandlerInfo} object.
 */
@Named
public class HandlerInfoJsonView extends AbstractJsonView {

  protected static final String TEXT_PLAIN_CONTENT_TYPE = "text/html; charset=UTF-8";

  @Override
  public <T> JsonElement getJSON(T bean) {
    HandlerInfo handlerInfo = (HandlerInfo) bean;
    JsonObject output = new JsonObject();
    output.addProperty("success", handlerInfo.isSuccess());

    if (!handlerInfo.isSuccess()) {
      Collection<String> errors = handlerInfo.getErrors();
      if (errors != null) {
        JsonArray jsonErrors = new JsonArray();
        for (String error : errors) {
          jsonErrors.add(new JsonPrimitive(error));
        }
        output.add("errors", jsonErrors);
      }
    }

    List<? extends HandlerInfo.Message> messages = handlerInfo.getMessages();
    if (!messages.isEmpty()) {
      JsonArray messagesOutput = new JsonArray();
      for (HandlerInfo.Message message : messages) {
        JsonObject messageOutput = new JsonObject();
        messageOutput.addProperty("type", message.getType());
        messageOutput.addProperty("text", message.getText());
        String path = message.getPath();
        if (path != null) {
          messageOutput.addProperty("path", path);
        }
        messagesOutput.add(messageOutput);
      }
      output.add("messages", messagesOutput);
    }

    Model model = handlerInfo.getModel();
    output.addProperty("id", model == null ? null : model.getId());
    output.addProperty("link", handlerInfo.getLink());
    return output;
  }

  @Override
  public String getContentType() {
    return TEXT_PLAIN_CONTENT_TYPE;
  }

}
