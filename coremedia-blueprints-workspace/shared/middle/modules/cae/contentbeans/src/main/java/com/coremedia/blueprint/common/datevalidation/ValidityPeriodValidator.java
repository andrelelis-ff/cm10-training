package com.coremedia.blueprint.common.datevalidation;

import com.coremedia.blueprint.cae.search.Condition;
import com.coremedia.blueprint.cae.search.SearchFilterProvider;
import com.coremedia.blueprint.cae.search.Value;
import com.coremedia.blueprint.cae.search.solr.SolrSearchFormatHelper;
import com.coremedia.blueprint.common.preview.PreviewDateFormatter;
import com.coremedia.blueprint.common.services.validation.AbstractValidator;
import com.coremedia.blueprint.common.util.ContextAttributes;
import com.coremedia.cache.Cache;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This validator checks if an item is an instance of {@link ValidityPeriod} and, if so, it is also within the preview date, if any.
 */
public class ValidityPeriodValidator extends AbstractValidator<ValidityPeriod> implements SearchFilterProvider<Condition> {

  private static final Logger LOG = LoggerFactory.getLogger(ValidityPeriodValidator.class);

  public static final String REQUEST_PARAMETER_PREVIEW_DATE = "previewDate";
  public static final String REQUEST_ATTRIBUTE_PREVIEW_DATE = "previewDateObj";

  private static final int INTERVAL = 5;
  private boolean preview;

  // --- spring config -------------------------------------------------------------------------------------------------

  @org.springframework.beans.factory.annotation.Value("${cae.is.preview}")
  public void setPreview(boolean preview) {
    this.preview = preview;
  }

  /**
   * This Validator validates classes implementing the ValidityPeriod interface
   */
  @Override
  public boolean supports(Class clazz) {
    return ValidityPeriod.class.isAssignableFrom(clazz);
  }

  @Override
  protected Predicate<ValidityPeriod> createPredicate() {
    return new ValidationPeriodPredicate(getPreviewDate());
  }

  @Override
  protected void addCustomDependencies(List<? extends ValidityPeriod> result) {
    if (!isValidityPeriodUsed(result)) {
      return;
    }

    if (preview) {
      // we don't want to cache anything in preview if somewhere a validation period is used
      // (the reason is: it could influence the validity decision at any time if later a previewdate is used)
      Cache.uncacheable();
    } else {
      Calendar validTime = getPreviewDate();
      Optional<Calendar> validUntil = findNearestDate(result, validTime);
      if (validUntil.isPresent()) {
        // set a timed dependency if a date is available (the minimal time that the list could be cached)
        Date validUntilTime = validUntil.get().getTime();
        Cache.cacheUntil(validUntilTime);
        if (LOG.isDebugEnabled()) {
          String chosenDateStr = FastDateFormat.getInstance("dd.MM.yyyy HH:mm:ss.SSS").format(validUntilTime);
          LOG.debug("Caching these items: '{}' until '{}'", result, chosenDateStr);
        }
      }
    }
  }

  @VisibleForTesting
  boolean isValidityPeriodUsed(@NonNull List<? extends ValidityPeriod> result) {
    //noinspection ConstantConditions
    return result.stream()
            // do not refactor this predicate to ValidityPeriod.class::isInstance - javac will optimize it to a simple null check!
            .filter((Predicate<Object>) o -> o instanceof ValidityPeriod) // be robust against type unsafe usage, filters nulls
            .map(ValidityPeriod.class::cast)
            .anyMatch(vp -> vp.getValidFrom() != null || vp.getValidTo() != null);
  }

  @NonNull
  @VisibleForTesting
  Optional<Calendar> findNearestDate(@NonNull List<? extends ValidityPeriod> allItems, @NonNull Calendar validTime) {
    LOG.debug("Searching the nearest date for these items: {}", allItems);

    Function<ValidityPeriod, Calendar> contentToEarliestValidationDate
            = vp -> EarliestValidationDateFinder.findNextDate(validTime, vp).orElse(null);

    //noinspection ConstantConditions
    return allItems.stream()
            // do not refactor this predicate to ValidityPeriod.class::isInstance - javac will optimize it to a simple null check!
            .filter((Predicate<Object>) o -> o instanceof ValidityPeriod) // be robust against type unsafe usage
            .map(contentToEarliestValidationDate)
            .filter(Objects::nonNull)
            .min(Comparator.naturalOrder());  // choose the earliest date
  }

  @NonNull
  private static Calendar getPreviewDate() {
    //is previewDate stored in the request attributes?
    Optional<Calendar> previewDateFromReqAttr = ContextAttributes
            .findRequestAttribute(REQUEST_ATTRIBUTE_PREVIEW_DATE, Calendar.class);
    if (previewDateFromReqAttr.isPresent()) {
      return previewDateFromReqAttr.get();
    }

    //if not stored in the request attributes
    //retrieve the previewDate from the request parameter
    Calendar previewDateFromReqParam = getPreviewDateFromRequestParameter();

    // store previewDate in the request attributes for the following checks within this request
    setPreviewDate(previewDateFromReqParam);

    return previewDateFromReqParam;
  }

  @NonNull
  private static Calendar getPreviewDateFromRequestParameter() {
    String previewDateText = ContextAttributes.findRequestParameter(REQUEST_PARAMETER_PREVIEW_DATE).orElse(null);
    return parsePreviewDateFromRequestParameter(previewDateText)
            .orElseGet(Calendar::getInstance);
  }

  @NonNull
  @VisibleForTesting
  static Optional<Calendar> parsePreviewDateFromRequestParameter(@Nullable String previewDateText) {
    return Optional.ofNullable(previewDateText)
            .flatMap(PreviewDateFormatter::parse)
            .map(GregorianCalendar::from);
  }

  private static void setPreviewDate(@NonNull Calendar previewDate) {
    ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (requestAttributes != null) {
      requestAttributes.setAttribute(REQUEST_ATTRIBUTE_PREVIEW_DATE, previewDate, ServletRequestAttributes.SCOPE_REQUEST);
    }
  }

  @Override
  public List<Condition> getFilter(boolean isPreview) {
    Calendar date = isPreview ? getPreviewDate() : Calendar.getInstance();
    date = getDateRounded(date, INTERVAL);
    String formattedDate = SolrSearchFormatHelper.calendarToString(date);

    List<Condition> conditions = new ArrayList<>();

    // condition for valid from
    Condition validFrom = Condition.is("validfrom",
            Value.exactly(SolrSearchFormatHelper.fromPastToValue(formattedDate))
    );
    // condition for valid to
    Condition validTo = Condition.is("validto",
            Value.exactly(SolrSearchFormatHelper.fromValueIntoFuture(formattedDate))
    );

    conditions.add(validFrom);
    conditions.add(validTo);

    return conditions;
  }

  @SuppressWarnings("SameParameterValue")
  @NonNull
  private static Calendar getDateRounded(@NonNull Calendar calendar, int interval) {
    Calendar result = (Calendar) calendar.clone();
    int minutes = result.get(Calendar.MINUTE);
    int mod = interval - (minutes % interval);
    if (mod == interval) {
      mod -= interval;
    }
    minutes += mod;
    result.set(Calendar.MINUTE, minutes);
    result.set(Calendar.SECOND, 0);
    result.set(Calendar.MILLISECOND, 0);
    return result;
  }
}
