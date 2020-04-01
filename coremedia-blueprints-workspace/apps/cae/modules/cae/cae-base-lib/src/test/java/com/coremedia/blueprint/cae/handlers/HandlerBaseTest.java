package com.coremedia.blueprint.cae.handlers;

import com.coremedia.objectserver.view.ViewUtils;
import com.coremedia.objectserver.web.HandlerHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class HandlerBaseTest {

  @Spy
  private HandlerBase handlerBase;

  @Mock
  private HttpServletResponse response;

  @Before
  public void setup() {
    handlerBase.setSingleNode(false);
  }

  @Test
  public void isUpToDate() {
    ModelAndView modelAndView = handlerBase.doCreateModelWithView(true, this, null, null, response);
    assertThat(modelAndView)
      .isNotNull()
      .satisfies(mav -> {
        assertThat(mav.getModel())
          .containsOnlyKeys(HandlerHelper.MODEL_ROOT)
          .containsValue(this);
        assertThat(mav.getViewName()).isEqualTo(ViewUtils.DEFAULT_VIEW);
      });

    verifyZeroInteractions(response);
  }

  @Test
  public void sendRedirect() {
    handlerBase.setSingleNode(true);
    ModelAndView modelAndView = handlerBase.doCreateModelWithView(false, this, null, null, response);
    assertThat(modelAndView)
      .isNotNull()
      .satisfies(mav -> {
        assertThat(mav.getModel())
          .containsOnlyKeys(HandlerHelper.MODEL_ROOT)
          .containsValue(this);
        assertThat(mav.getViewName()).isEqualTo("redirect:" + ViewUtils.DEFAULT_VIEW);
      });
    verifyZeroInteractions(response);
  }

  @Test
  public void doNotSendRedirect() {
    ModelAndView modelAndView = handlerBase.doCreateModelWithView(false, this, null, null, response);
    assertThat(modelAndView)
      .isNotNull()
      .satisfies(mav -> {
        assertThat(mav.getModel())
          .containsOnlyKeys(HandlerHelper.MODEL_ROOT)
          .containsValue(this);
        assertThat(mav.getViewName()).isEqualTo(ViewUtils.DEFAULT_VIEW);
      });
    verify(response).setHeader("Cache-Control", "no-store");
  }

}
