package com.coremedia.blueprint.analytics.elastic.contentbeans;

import com.coremedia.id.IdProvider;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.List;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.base.Predicates.isNull;
import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;
import static com.google.common.collect.ImmutableList.copyOf;

/**
 * Extension class for beans of document type "CMALXBaseList".
 */
public abstract class CMALXBaseListImpl<V> extends CMALXBaseListBase<V> {

  private static final Logger LOG = LoggerFactory.getLogger(CMALXBaseListImpl.class);

  private IdProvider idProvider;

  @Override
  public int getMaxLength() {
    final int length = super.getMaxLength();
    if (length <= 0) {
      return DEFAULT_MAX_LENGTH;
    }
    return length;
  }

  @Override
  public String getAnalyticsProvider() {
    final String analyticsProvider = getContent().getString(ANALYTICS_PROVIDER);
    if (analyticsProvider == null) {
      return getSettingsService().setting(ANALYTICS_PROVIDER, String.class, this);
    }
    return analyticsProvider;
  }

  @Override
  public int getTimeRange() {
    final int range = getContent().getInt(TIME_RANGE);
    if (range <= 0) {
      return DEFAULT_TIME_RANGE;
    }
    return range;
  }

  @Required
  public void setIdProvider(IdProvider idProvider) {
    if (idProvider == null) {
      throw new IllegalArgumentException("supplied 'idProvider' must not be null");
    }
    this.idProvider = idProvider;
  }

  /**
   * @return The unmodified tracked events, which are custom Strings (depending on what you tracked)
   */
  protected final List<Object> getTrackedItemsUnfiltered() {
    final Predicate<Object> notNullOrUnknown = not(or(isNull(), instanceOf(IdProvider.UnknownId.class)));
    return copyOf(Iterables.filter(Lists.transform(getTrackedObjects(), new ParseUsingIdProvider()), notNullOrUnknown));
  }

  public IdProvider getIdProvider() {
    return idProvider;
  }

  private class ParseUsingIdProvider implements Function<String, Object> {
    @Nullable
    @Override
    public Object apply(@Nullable String input) {
      if (input != null) {
        try {
          return getIdProvider().parseId(input);
        } catch (IllegalArgumentException e) {
          LOG.warn("Could not parse id: " + input, e);
          return null;
        }
      }
      return null;
    }
  }
}
