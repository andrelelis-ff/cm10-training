package com.coremedia.blueprint.elastic.social.cae.flows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageResolver;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.context.NoSuchMessageException;
import org.springframework.webflow.execution.RequestContext;

public final class MessageHelper {
  private static final Logger LOG = LoggerFactory.getLogger(MessageHelper.class);

  private MessageHelper() {
  }

  public static void addInfoMessage(RequestContext context, String key) {
    context.getMessageContext().addMessage(new MessageBuilder().info().code(key).build());
  }

  public static void addErrorMessage(RequestContext context, String key) {
    addMessage(context, key, new MessageBuilder().error().code(key).build());
  }

  public static void addErrorMessage(RequestContext context, String key, Object... args) {
    addMessage(context, key, new MessageBuilder().error().code(key).arg(args).build());
  }

  public static void addErrorMessageWithSource(RequestContext context, String key, String source) {
    addMessage(context, key, new MessageBuilder().error().code(key).source(source).build());
  }

  public static void addErrorMessageWithSource(RequestContext context, String key, String source, Object... args) {
    addMessage(context, key, new MessageBuilder().error().code(key).source(source).args(args).build());
  }

  public static void addErrorMessageWithSource(ValidationContext context, String key, String source) {
    addMessage(context, key, new MessageBuilder().error().code(key).source(source).build());
  }

  public static void addErrorMessageWithSource(ValidationContext context, String key, String source, Object... args) {
    addMessage(context, key, new MessageBuilder().error().code(key).source(source).args(args).build());
  }

  /**
   * Helper method to apply the message.
   * Instead of a complete request fail, we want to return the key as message text instead.
   */
  private static void addMessage(ValidationContext context, String key, MessageResolver resolver) {
    try {
      context.getMessageContext().addMessage(resolver);
    }
    catch (NoSuchMessageException e) {
      context.getMessageContext().addMessage(new MessageBuilder().error().defaultText(key).build());
      LOG.warn("Failed to resolve message key: " + e.getMessage());
    }
  }

  /**
   * Helper method to apply the message.
   * Instead of a complete request fail, we want to return the key as message text instead.
   */
  private static void addMessage(RequestContext context, String key, MessageResolver resolver) {
    try {
      context.getMessageContext().addMessage(resolver);
    }
    catch (NoSuchMessageException e) {
      context.getMessageContext().addMessage(new MessageBuilder().error().defaultText(String.valueOf(key)).build());
      LOG.warn("Failed to resolve message key: " + e.getMessage());
    }
  }
}
