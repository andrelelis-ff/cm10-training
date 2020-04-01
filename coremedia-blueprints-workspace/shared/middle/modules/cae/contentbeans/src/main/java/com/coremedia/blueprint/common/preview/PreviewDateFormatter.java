package com.coremedia.blueprint.common.preview;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@DefaultAnnotation(NonNull.class)
public class PreviewDateFormatter {

  private static final Logger LOG = LoggerFactory.getLogger(PreviewDateFormatter.class);

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm VV");

  private PreviewDateFormatter() {
  }

  public static String format(ZonedDateTime dateTime) {
    return dateTime.format(FORMATTER);
  }

  public static Optional<ZonedDateTime> parse(String text) {
    try {
      ZonedDateTime zonedDateTime = ZonedDateTime.parse(text, FORMATTER);
      return Optional.of(zonedDateTime);
    } catch (DateTimeParseException e) {
      LOG.warn("Could not parse preview date '{}'.", text, e);
      return Optional.empty();
    }
  }
}
