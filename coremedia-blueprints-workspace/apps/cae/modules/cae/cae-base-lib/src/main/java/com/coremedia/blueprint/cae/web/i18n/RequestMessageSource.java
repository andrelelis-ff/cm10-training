package com.coremedia.blueprint.cae.web.i18n;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * A message source implementation that delegates to a message source that has probably been
 * {@link #setMessageSource(org.springframework.context.MessageSource, javax.servlet.http.HttpServletRequest) stored} as
 * a servlet request attributes. If no such source is available or if the message couldn't be resolved, then
 * a {@link #setParentMessageSource(org.springframework.context.MessageSource) parent source} is used.
 */
public class RequestMessageSource implements HierarchicalMessageSource {

  public static final String MESSAGESOURCE_ATTRIBUTE = MessageSource.class.getName();

  private MessageSource parent;

  /**
   * Stores a message source as a servlet request attribute
   *
   * @param source  The message source
   * @param request the request
   */
  public static void setMessageSource(MessageSource source, HttpServletRequest request) {
    request.setAttribute(MESSAGESOURCE_ATTRIBUTE, source);
  }

  @Override
  public void setParentMessageSource(MessageSource parent) {
    this.parent = parent;
  }

  @Override
  public MessageSource getParentMessageSource() {
    return parent;
  }

  @Override
  public String getMessage(@NonNull String code, Object[] args, String defaultMessage, @NonNull Locale locale) {
    MessageSource requestMessageSource = getRequestMessageSource();

    return findMessage(requestMessageSource, code, args, locale)
            .or(() -> findMessage(parent, code, args, locale))
            .orElseGet(() -> {
              if (requestMessageSource != null) {
                return requestMessageSource.getMessage(code, args, defaultMessage, locale);
              } else if (parent != null) {
                return parent.getMessage(code, args, defaultMessage, locale);
              } else {
                MessageFormat format = new MessageFormat(defaultMessage != null ? defaultMessage : "", locale);
                return format.format(args);
              }
            });
  }

  @NonNull
  @Override
  public String getMessage(@NonNull String code, Object[] args, @NonNull Locale locale) {
    MessageSource requestMessageSource = getRequestMessageSource();

    return findMessage(requestMessageSource, code, args, locale)
            .or(() -> findMessage(parent, code, args, locale))
            .orElseThrow(() -> new NoSuchMessageException(code, locale));
  }

  @NonNull
  @Override
  public String getMessage(@NonNull MessageSourceResolvable resolvable, @NonNull Locale locale) {
    MessageSource requestMessageSource = getRequestMessageSource();

    return findMessage(requestMessageSource, resolvable, locale)
            .or(() -> findMessage(parent, resolvable, locale))
            .or(() -> Optional.ofNullable(resolvable.getDefaultMessage()))
            .orElseThrow(() -> {
              String firstCode = getFirstCode(resolvable).orElse("");
              throw new NoSuchMessageException(firstCode, locale);
            });
  }

  /**
   * Provides the message source that has been stored in the request
   *
   * @return The message source or null if none available
   */
  @Nullable
  private static MessageSource getRequestMessageSource() {
    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

    if (attributes == null) {
      return null;
    }

    return (MessageSource) attributes.getAttribute(MESSAGESOURCE_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
  }

  @NonNull
  private static Optional<String> findMessage(@Nullable MessageSource source, @NonNull String code,
                                              @Nullable Object[] args, @NonNull Locale locale) {
    if (source == null) {
      return Optional.empty();
    }

    try {
      String message = source.getMessage(code, args, locale);
      return Optional.of(message);
    } catch (NoSuchMessageException e) {
      return Optional.empty();
    }
  }

  @NonNull
  private static Optional<String> findMessage(@Nullable MessageSource source,
                                              @NonNull MessageSourceResolvable resolvable, @NonNull Locale locale) {
    if (source == null) {
      return Optional.empty();
    }

    try {
      String message = source.getMessage(resolvable, locale);
      return Optional.of(message);
    } catch (NoSuchMessageException e) {
      return Optional.empty();
    }
  }

  @NonNull
  private static Optional<String> getFirstCode(@NonNull MessageSourceResolvable resolvable) {
    String[] codes = resolvable.getCodes();

    if (codes == null || codes.length == 0) {
      return Optional.empty();
    }

    return Optional.ofNullable(codes[0]);
  }
}
