package com.coremedia.blueprint.elastic.social.cae.view;

import com.coremedia.blueprint.elastic.social.cae.controller.HandlerInfo;
import com.coremedia.blueprint.elastic.social.cae.guid.GuidCookieHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link com.coremedia.blueprint.elastic.social.cae.view.HandlerInfoJsonView}.
 */
@RunWith(MockitoJUnitRunner.class)
public class HandlerInfoJsonViewTest extends AbstractJsonViewTest {
  private static final String LINK = "/blueprint/comments?moreComments=true#id_";
  HandlerInfoJsonView handlerInfoJsonView = new HandlerInfoJsonView();

  private MockHttpServletResponse response = new MockHttpServletResponse();

  @Mock
  private HttpServletRequest httpServletRequest;

  @Mock
  private HttpServletResponse httpServletResponse;

  private StringWriter writer = new StringWriter();

  @Before
  public void setup() {
    GuidCookieHandler.setCurrentGuid("123");
  }

  @Test
  public void comment() {
    HandlerInfo handlerInfo = new HandlerInfo();
    handlerInfo.setLink(LINK);
    handlerInfo.setSuccess(true);

    handlerInfoJsonView.render(handlerInfo, null, writer, httpServletRequest, response);

    assertEquals(HandlerInfoJsonView.TEXT_PLAIN_CONTENT_TYPE, response.getContentType());
    assertJsonEquals("" +
            "{success: true," +
            "link: \"" + LINK + "\"}",
            writer.toString());
  }

  @Test
  public void commentError() {
    HandlerInfo handlerInfo = new HandlerInfo();
    handlerInfo.setLink(LINK);
    handlerInfo.setSuccess(false);
    handlerInfo.setErrors(Collections.singletonList("message"));

    handlerInfoJsonView.render(handlerInfo, null, writer, httpServletRequest, response);

    assertEquals(HandlerInfoJsonView.TEXT_PLAIN_CONTENT_TYPE, response.getContentType());
    assertJsonEquals("" +
            "{success: false," +
            "link: \"" + LINK + "\"," +
            "errors: [\"message\"]}",
            writer.toString());
  }

  @Test
  public void commentMessages() {
    HandlerInfo handlerInfo = new HandlerInfo();
    handlerInfo.setLink(LINK);
    handlerInfo.setSuccess(true);
    handlerInfo.addMessage("ab", "cd", "ef");

    handlerInfoJsonView.render(handlerInfo, null, writer, httpServletRequest, response);

    assertEquals(HandlerInfoJsonView.TEXT_PLAIN_CONTENT_TYPE, response.getContentType());
    assertJsonEquals("" +
                    "{success: true," +
                    "messages: [{type: \"ab\", text: \"ef\", path: \"cd\"}],"+
                    "link: \"" + LINK + "\"}",
            writer.toString());
  }
}
