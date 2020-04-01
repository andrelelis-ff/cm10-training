package com.coremedia.ecommerce.studio.rest.exception;

import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.livecontext.ecommerce.common.CommerceRemoteException;
import com.coremedia.livecontext.ecommerce.common.UnauthorizedException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommerceExceptionMapperTest {

  private CommerceExceptionMapper exceptionMapper;

  @Before
  public void setUp() {
    exceptionMapper = new CommerceExceptionMapper();
  }

  private HttpServletRequest buildRequest() {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getMethod()).thenReturn("POST");
    when(request.getContentType()).thenReturn(MULTIPART_FORM_DATA);

    return request;
  }

  @Test
  public void anyOtherCommerceException() {
    CommerceException exception = new CommerceException("someMessage");

    testToResponse(exception, "LC-01001");
  }

  @Test
  public void commerceRemoteException() {
    CommerceException exception = new CommerceRemoteException("someMessage", 500, "someErrorCode", "someErrorKey");

    testToResponse(exception, "LC-01002");
  }

  @Test
  public void unauthorizedException() {
    CommerceException exception = new UnauthorizedException("someMessage", 500);

    testToResponse(exception, "LC-01003");
  }

  private void testToResponse(CommerceException exception, String expectedErrorCode) {
    ResponseEntity actual = exceptionMapper.toResponse(exception, buildRequest());

    assertErrorCode(actual, expectedErrorCode);
  }

  private static void assertErrorCode(ResponseEntity actual, String expectedErrorCode) {
    assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);

    String responseBody = (String) actual.getBody();

    assertThat(responseBody).containsPattern("\"errorCode\" : \"" + expectedErrorCode + "\"");
    assertThat(responseBody).containsPattern("\"status\" : 410");
  }
}
