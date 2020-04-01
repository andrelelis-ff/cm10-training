package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.cap.multisite.SitesService;
import com.coremedia.elastic.social.api.comments.Comment;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class HandlerInfoTest {

  private String messageType = "error";
  private String path = "comment";
  private String text = "text";

  @Test
  public void test() {
    SitesService sitesService = mock(SitesService.class);

    Comment comment = mock(Comment.class);
    String link = "link";

    HandlerInfo handlerInfo = new HandlerInfo();
    handlerInfo.addMessage(messageType, path, text);
    handlerInfo.setSuccess(false);
    handlerInfo.setModel(comment);
    handlerInfo.setLink(link);

    assertFalse(handlerInfo.isSuccess());
    assertEquals(comment, handlerInfo.getModel());
    assertEquals(link, handlerInfo.getLink());
    List<HandlerInfo.Message> messages = handlerInfo.getMessages();
    assertEquals(1, messages.size());
    HandlerInfo.Message message = messages.get(0);
    assertEquals(messageType, message.getType());
    assertEquals(path, message.getPath());
    assertEquals(text, message.getText());
  }

  @Test
  public void testMessage() {
    HandlerInfo.Message message = new HandlerInfo.Message(path, text, messageType);
    assertEquals(messageType, message.getType());
    assertEquals(path, message.getPath());
    assertEquals(text, message.getText());

    String anotherMessageType = "success";
    String noPath = null;
    String anotherText = " another text";

    message.setPath(noPath);
    message.setText(anotherText);
    message.setType(anotherMessageType);

    assertEquals(anotherMessageType, message.getType());
    assertNull(message.getPath());
    assertEquals(anotherText, message.getText());
  }
}
