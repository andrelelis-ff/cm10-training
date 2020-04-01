/*
 * Copyright (c) 2011, CoreMedia AG, Hamburg. All rights reserved.
 */
package com.coremedia.blueprint.personalization.preview;

import com.coremedia.blueprint.common.preview.PreviewDateFormatter;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.PropertyProvider;
import com.coremedia.personalization.context.collector.AbstractContextSource;
import com.coremedia.personalization.context.collector.SystemDateTimeContext;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;

/**
 * Overrides the current date and time by getting a test-date-and-time from the following "sources":
 * <ol>
 * <li>parsing value of a parameter {@link #REQUEST_PARAMETER_PREVIEW_DATE} from current {@link javax.servlet.http.HttpServletRequest} -
 * set by the Studio timetravel selector</li>
 * <li>otherwise, check if there is already a context property "system.dateandtime" set by a persona preview</li>
 * <li>otherwise, use {@link java.util.Calendar#getInstance()}</li>
 * </ol>
 * With the given "now", this class adds a {@link com.coremedia.personalization.context.collector.SystemDateTimeContext}
 * to the {@ink ContextCollection}
 */
public class DateTimeTestContextSource extends AbstractContextSource {

  private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeTestContextSource.class);

  /**
   * default personalization context name to read/write context data
   */
  public static String DEFAULT_CONTEXT_NAME = "system";

  private String contextName = DEFAULT_CONTEXT_NAME;

  /**
   * name of the request parameter for timetravel
   */
  public static final String REQUEST_PARAMETER_PREVIEW_DATE = "previewDate";

  @Override
  public void preHandle(HttpServletRequest request, HttpServletResponse response, ContextCollection contextCollection) {
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

    Calendar now = getNow(attributes, contextCollection);

    SystemDateTimeContext context = new SystemDateTimeContext(now);

    if (contextCollection != null) {
      contextCollection.setContext(contextName, context);
    }
  }

  @NonNull
  private Calendar getNow(@Nullable ServletRequestAttributes attributes,
                          @Nullable ContextCollection contextCollection) {
    Calendar now = null;

    // first try to get preview date and time from a set timetravel URL parameter
    if (attributes != null) {
      now = getPreviewDateFromRequestParameter(attributes.getRequest()).orElse(null);
    }

    // if not set as timetravel, try to get preview dateandtime from
    // already parsed p13n (test) context - concrete: from a persona
    if (now == null && contextCollection != null) {
      @SuppressWarnings("PersonalData" /* The system context is not personal data. */)
      Object o = contextCollection.getContext(contextName);
      if (o instanceof PropertyProvider) {
        PropertyProvider systemContext = (PropertyProvider) o;
        @SuppressWarnings("PersonalData" /* This is not personal data. */)
        Calendar dateandtime = (Calendar) systemContext.getProperty("dateandtime");
        now = dateandtime;
      }
    }

    if (now == null) {
      now = Calendar.getInstance();
    }

    return now;
  }

  /**
   * override context name to read/write date and time data from/to
   *
   * @param contextName the name of the perso context (default: {@link #DEFAULT_CONTEXT_NAME})
   */
  public void setContextName(String contextName) {
    this.contextName = contextName;
  }

  /**
   * @param request the given request
   * @return preview date, or nothing if no preview date is set
   */
  @NonNull
  private static Optional<Calendar> getPreviewDateFromRequestParameter(@NonNull HttpServletRequest request) {
    return Optional.ofNullable(request.getParameter(REQUEST_PARAMETER_PREVIEW_DATE))
            .flatMap(PreviewDateFormatter::parse)
            .map(GregorianCalendar::from);
  }
}
