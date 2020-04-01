package com.coremedia.blueprint.cae.exception.handler;

import com.coremedia.objectserver.view.ViewUtils;
import com.coremedia.objectserver.web.HandlerHelper;
import com.coremedia.objectserver.web.HttpError;
import org.junit.Test;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link ConversionFailedExceptionHandler}.
 */
public class ConversionFailedExceptionHandlerTest {
  private ErrorAndExceptionHandler handler = new ConversionFailedExceptionHandler();
  private ConversionFailedException stringToIntConversionFailed = new ConversionFailedException(
    TypeDescriptor.valueOf(String.class),
    TypeDescriptor.valueOf(Integer.class),
    "abc", new NumberFormatException("test"));

  private MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
  private MockHttpServletResponse response = new MockHttpServletResponse();

  private static final String VIEW = "IGNORED";

  @Test
  public void extractConversionFailedException() {
    assertSame(stringToIntConversionFailed, new ConversionFailedExceptionHandler().resolveException(
      new TypeMismatchException("abc", Integer.class, stringToIntConversionFailed)));
  }

  @Test
  public void ignoreNonTypeMismatchException() {
    assertNull("IllegalArgumentException",
      handler.handleException(VIEW, new IllegalArgumentException("test"), request, response));
  }

  @Test
  public void ignoreCauseOtherThanConversionFailedException() {
    assertNull("cause is IllegalArgumentException", handler.handleException(VIEW,
      new TypeMismatchException("value", Integer.class, new IllegalArgumentException("test")),
      request, response));
  }

  @Test
  public void ignoreNullException() {
    assertNull("ignore null exception", handler.handleException(VIEW, null, request, response));
  }

  @Test
  public void errorModel() {
    ModelAndView mav = handler.handleException(VIEW,
      new TypeMismatchException("value", Integer.class, stringToIntConversionFailed),
      request, response);

    assertEquals("status code in response", HttpServletResponse.SC_NOT_FOUND, response.getStatus());

    Object self = HandlerHelper.getRootModel(mav);
    assertTrue("expected HttpError model", self instanceof HttpError);
    assertEquals("status code in model", HttpServletResponse.SC_NOT_FOUND, ((HttpError) self).getErrorCode());

    assertEquals("expected default view", ViewUtils.DEFAULT_VIEW, mav.getViewName());
  }
}
