package com.coremedia.blueprint.cae.web.i18n;

import org.junit.After;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class RequestMessageSourceTest {

  private static final String LOCALIZED_MESSAGE = "Localized Message";
  private static final String PARENT_LOCALIZED_MESSAGE = "Parent Localized Message";

  private static final String DEFAULT_MESSAGE = "Default Message";

  private static final String DEFAULT_MESSAGE_FORMAT = "Default Message {0}";
  private static final Object[] DEFAULT_MESSAGE_ARGS = new Object[]{123};
  private static final String DEFAULT_MESSAGE_FORMAT_EXPECTED = "Default Message 123";

  private static final String RESOLVABLE_DEFAULT_MESSAGE = "resolvableDefaultMessage";

  private final static MessageSourceResolvable RESOLVABLE = new MyMessageSourceResolvable(RESOLVABLE_DEFAULT_MESSAGE);
  private final static MessageSourceResolvable RESOLVABLE_NULL_DEFAULT = new MyMessageSourceResolvable(null);

  private static final String MESSAGE_CODE = "messageCode";
  private static final String OTHER_MESSAGE_CODE = "otherMessageCode";

  private static final Object[] ARGS = new Object[]{};
  private static final Locale LOCALE = Locale.ROOT;

  private final static MessageSource CONTEXT_REQUEST_MESSAGE_SOURCE =
          mockMessageSource(LOCALIZED_MESSAGE);

  private final static MessageSource PARENT_MESSAGE_SOURCE =
          mockMessageSource(PARENT_LOCALIZED_MESSAGE);

  @After
  public void tearDown() {
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  public void testByCodeDirect() {
    RequestMessageSource toTest = setupEnvironment(CONTEXT_REQUEST_MESSAGE_SOURCE, PARENT_MESSAGE_SOURCE);

    assertEquals("Messages should be equal", LOCALIZED_MESSAGE, toTest.getMessage(MESSAGE_CODE, ARGS, LOCALE));
  }

  @Test
  public void testByCodeParent() {
    RequestMessageSource toTest = setupEnvironment(null, PARENT_MESSAGE_SOURCE);

    assertEquals("Messages should be equal", PARENT_LOCALIZED_MESSAGE, toTest.getMessage(MESSAGE_CODE, ARGS, LOCALE));
  }

  @Test(expected = NoSuchMessageException.class)
  public void testByCodeException() {
    RequestMessageSource toTest = setupEnvironment(null, null);
    toTest.getMessage(MESSAGE_CODE, ARGS, LOCALE);
  }

  @Test(expected = NoSuchMessageException.class)
  public void testByCodeNotFound() {
    RequestMessageSource toTest = setupEnvironment(CONTEXT_REQUEST_MESSAGE_SOURCE, PARENT_MESSAGE_SOURCE);
    toTest.getMessage(OTHER_MESSAGE_CODE, ARGS, LOCALE);
  }

  @Test
  public void testByCodeDefaultDirect() {
    RequestMessageSource toTest = setupEnvironment(CONTEXT_REQUEST_MESSAGE_SOURCE, PARENT_MESSAGE_SOURCE);

    assertEquals("Messages should be equal", LOCALIZED_MESSAGE, toTest.getMessage(MESSAGE_CODE, ARGS, DEFAULT_MESSAGE, LOCALE));
  }

  @Test
  public void testByCodeDefaultParent() {
    RequestMessageSource toTest = setupEnvironment(null, PARENT_MESSAGE_SOURCE);

    assertEquals("Messages should be equal", PARENT_LOCALIZED_MESSAGE, toTest.getMessage(MESSAGE_CODE, ARGS, DEFAULT_MESSAGE, LOCALE));
  }

  @Test
  public void testByCodeDefaultNoResourceGetDefault() {
    RequestMessageSource toTest = setupEnvironment(null, null);

    assertEquals("Messages should be equal", DEFAULT_MESSAGE, toTest.getMessage(MESSAGE_CODE, ARGS, DEFAULT_MESSAGE, LOCALE));
  }

  @Test
  public void testByCodeDefaultNoResourceGetDefaultFormat() {
    RequestMessageSource toTest = setupEnvironment(null, null);

    assertEquals("Messages should be equal", DEFAULT_MESSAGE_FORMAT_EXPECTED, toTest.getMessage(MESSAGE_CODE, DEFAULT_MESSAGE_ARGS, DEFAULT_MESSAGE_FORMAT, LOCALE));
  }

  @Test
  public void testByCodeDefaultNotFound() {
    RequestMessageSource toTest = setupEnvironment(CONTEXT_REQUEST_MESSAGE_SOURCE, PARENT_MESSAGE_SOURCE);

    assertEquals("Messages should be equal", DEFAULT_MESSAGE, toTest.getMessage(OTHER_MESSAGE_CODE, ARGS, DEFAULT_MESSAGE, LOCALE));
  }

  @Test
  public void testByCodeDefaultNotFound2() {
    RequestMessageSource toTest = setupEnvironment(null, PARENT_MESSAGE_SOURCE);

    assertEquals("Messages should be equal", DEFAULT_MESSAGE, toTest.getMessage(OTHER_MESSAGE_CODE, ARGS, DEFAULT_MESSAGE, LOCALE));
  }

  @Test
  public void testByCodeDefaultNotFound3() {
    RequestMessageSource toTest = setupEnvironment(null, null);

    assertEquals("Messages should be equal", DEFAULT_MESSAGE, toTest.getMessage(OTHER_MESSAGE_CODE, ARGS, DEFAULT_MESSAGE, LOCALE));
  }

  @Test
  public void testByResolvableDirect() {
    RequestMessageSource toTest = setupEnvironment(CONTEXT_REQUEST_MESSAGE_SOURCE, PARENT_MESSAGE_SOURCE);

    assertEquals("Messages should be equal", LOCALIZED_MESSAGE, toTest.getMessage(RESOLVABLE, LOCALE));
  }

  @Test(expected = NoSuchMessageException.class)
  public void testByResolvableDirectNotFound() {
    RequestMessageSource toTest = setupEnvironment(CONTEXT_REQUEST_MESSAGE_SOURCE, PARENT_MESSAGE_SOURCE);

    toTest.getMessage(RESOLVABLE_NULL_DEFAULT, LOCALE);
  }

  @Test
  public void testByResolvableParent() {
    RequestMessageSource toTest = setupEnvironment(null, PARENT_MESSAGE_SOURCE);

    assertEquals("Messages should be equal", PARENT_LOCALIZED_MESSAGE, toTest.getMessage(RESOLVABLE, LOCALE));
  }

  @Test
  public void testByResolvableDefault() {
    RequestMessageSource toTest = setupEnvironment(null, null);

    assertEquals("Messages should be equal", RESOLVABLE_DEFAULT_MESSAGE, toTest.getMessage(RESOLVABLE, LOCALE));
  }

  @Test(expected = NoSuchMessageException.class)
  public void testByResolvableDefaultNull() {
    RequestMessageSource toTest = setupEnvironment(null, null);

    toTest.getMessage(RESOLVABLE_NULL_DEFAULT, LOCALE);
  }

  private RequestMessageSource setupEnvironment(MessageSource contextSource, MessageSource parentSource) {

    // Prepare RequestContextHolder
    HttpServletRequest mockServletRequest = mock(HttpServletRequest.class);
    when(mockServletRequest.getAttribute(any(String.class))).thenReturn(contextSource);
    RequestAttributes attributes = new ServletRequestAttributes(mockServletRequest);
    RequestContextHolder.setRequestAttributes(attributes);

    // Prepare RequestMessageSourceToTest
    RequestMessageSource retVal = new RequestMessageSource();
    setRequestMessageSource(contextSource);
    retVal.setParentMessageSource(parentSource);

    return retVal;
  }

  private void setRequestMessageSource(MessageSource source) {
    RequestContextHolder.getRequestAttributes()
            .setAttribute(RequestMessageSource.MESSAGESOURCE_ATTRIBUTE, source, RequestAttributes.SCOPE_REQUEST);
  }

  private static MessageSource mockMessageSource(String giveBack) {
    MessageSource source = mock(MessageSource.class);
    when(source.getMessage(eq(MESSAGE_CODE), any(Object[].class), eq(LOCALE))).thenReturn(giveBack);
    when(source.getMessage(eq(OTHER_MESSAGE_CODE), any(Object[].class), eq(LOCALE))).thenThrow(NoSuchMessageException.class);
    when(source.getMessage(eq(MESSAGE_CODE), any(Object[].class), eq(DEFAULT_MESSAGE), eq(LOCALE))).thenReturn(giveBack);
    when(source.getMessage(eq(OTHER_MESSAGE_CODE), any(Object[].class), eq(DEFAULT_MESSAGE), eq(LOCALE))).thenReturn(DEFAULT_MESSAGE);
    when(source.getMessage(eq(RESOLVABLE), eq(LOCALE))).thenReturn(giveBack);
    when(source.getMessage(eq(RESOLVABLE_NULL_DEFAULT), eq(LOCALE))).thenThrow(NoSuchMessageException.class);
    return source;
  }

  static class MyMessageSourceResolvable implements MessageSourceResolvable {

    private final String defaultMessage;

    MyMessageSourceResolvable(String defaultMessage) {
      this.defaultMessage = defaultMessage;
    }

    @Override
    public String[] getCodes() {
      return new String[]{MESSAGE_CODE};
    }

    @Override
    public Object[] getArguments() {
      return new Object[]{};
    }

    @Override
    public String getDefaultMessage() {
      return defaultMessage;
    }
  }
}
