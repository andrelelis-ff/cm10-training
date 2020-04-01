package com.coremedia.livecontext.ecommerce.ibm.common;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractStoreContextProvider;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.common.InvalidContextException;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.login.WcCredentials;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.util.Currency;
import java.util.Locale;
import java.util.Map;

import static com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl.COMMERCE_SYSTEM_IS_UNAVAILABLE;

/**
 * Helper class to build an "IBM WCS conform" store context.
 * You do not have to know the exact keys if you use the helper method.
 * Use this class as static import.
 */
public class StoreContextHelper {

  private static final String CREDENTIALS = "credentials";

  private StoreContextHelper() {
  }

  @NonNull
  public static StoreContext getCurrentContextFor(@Nullable Locale locale) {
    StoreContext currentStoreContext = CurrentStoreContext.get();

    // locale can be null if the default locale is not set for commerce beans
    // in such a case we return the current context (a warning should be logged from caller)
    if (locale == null) {
      return currentStoreContext;
    }

    return buildContext(currentStoreContext, locale);
  }

  @NonNull
  private static StoreContextImpl buildContext(@NonNull StoreContext currentStoreContext, @NonNull Locale locale) {
    IbmStoreContextBuilder builder = IbmStoreContextBuilder
            .from(currentStoreContext.getConnection(), currentStoreContext.getSiteId());

    String storeId = currentStoreContext.getStoreId();
    if (storeId != null) {
      if (StringUtils.isBlank(storeId)) {
        throw new InvalidContextException("Store ID must not be blank.");
      }

      builder.withStoreId(storeId);
    }

    String storeName = currentStoreContext.getStoreName();
    if (storeName != null) {
      if (StringUtils.isBlank(storeName)) {
        throw new InvalidContextException("Store name must not be blank.");
      }

      builder.withStoreName(storeName);
    }

    currentStoreContext.getCatalogId().ifPresent(builder::withCatalogId);

    Currency currency = currentStoreContext.getCurrency();
    if (currency != null) {
      builder.withCurrency(currency);
    }

    return builder
            .withLocale(locale)
            .withCatalogAlias(currentStoreContext.getCatalogAlias())
            .withWorkspaceId(currentStoreContext.getWorkspaceId().orElse(null))
            .withWcsVersion(getWcsVersion(currentStoreContext))
            .build();
  }

  /**
   * Gets the store id from the given store context.
   *
   * @param context the store context
   * @return the store id
   * @throws com.coremedia.livecontext.ecommerce.common.InvalidContextException if the store id is invalid (missing or wrong type)
   */
  @NonNull
  public static String getStoreId(@NonNull StoreContext context) {
    String storeId = context.getStoreId();

    if (storeId == null) {
      throw new InvalidContextException("Store ID missing in store context (" + formatContext(context) + ")");
    }

    return storeId;
  }

  /**
   * Gets the store name from the given store context.
   *
   * @param context the store context
   * @return the store name
   * @throws com.coremedia.livecontext.ecommerce.common.InvalidContextException if the store id is invalid (missing or wrong type)
   */
  @NonNull
  public static String getStoreName(@NonNull StoreContext context) {
    String storeName = context.getStoreName();

    if (storeName == null) {
      throw new InvalidContextException("Store name missing in store context (" + formatContext(context) + ")");
    }

    return storeName;
  }

  /**
   * Gets the locale from the given store context.
   *
   * @param context the store context
   * @return the locale
   * @throws com.coremedia.livecontext.ecommerce.common.InvalidContextException if the store id is invalid (missing or wrong type)
   */
  @NonNull
  public static Locale getLocale(@NonNull StoreContext context) {
    Locale locale = context.getLocale();

    if (locale == null) {
      throw new InvalidContextException("Locale missing in store context (" + formatContext(context) + ")");
    }

    return locale;
  }

  /**
   * Gets the currency from the given store context.
   *
   * @param context the store context
   * @return the currency
   * @throws com.coremedia.livecontext.ecommerce.common.InvalidContextException if the store id is invalid (missing or wrong type)
   */
  @NonNull
  public static Currency getCurrency(@NonNull StoreContext context) {
    Currency currency = context.getCurrency();

    if (currency == null) {
      throw new InvalidContextException("Currency missing in store context (" + formatContext(context) + ")");
    }

    return currency;
  }

  /**
   * Gets the wcs version from the given store context or WCS_VERSION_UNKNOWN if no version has been set.
   *
   * @param context the store context
   * @return the version
   */
  @NonNull
  public static WcsVersion getWcsVersion(@NonNull StoreContext context) {
    WcsVersion version = (WcsVersion) ((StoreContextImpl) context)
            .get(AbstractStoreContextProvider.CONFIG_KEY_WCS_VERSION);

    if (version == null) {
      throw new InvalidContextException("WCS version missing in store context (" + formatContext(context) + ")");
    }

    return version;
  }

  public static boolean isCommerceSystemUnavailable(@NonNull StoreContext context) {
    Object value = ((StoreContextImpl) context).get(COMMERCE_SYSTEM_IS_UNAVAILABLE);
    return value instanceof Boolean && (Boolean) value;
  }

  public static void setCommerceSystemIsUnavailable(@NonNull StoreContext context, boolean isUnavailable) {
    ((StoreContextImpl) context).put(COMMERCE_SYSTEM_IS_UNAVAILABLE, isUnavailable);
  }

  public static void setCredentials(@NonNull StoreContext context, WcCredentials credentials) {
    ((StoreContextImpl) context).put(CREDENTIALS, credentials);
  }

  /**
   * Gets true if the dynamic pricing is enabled that leads to separate personalized price calls.
   * Default: false (if not configured, it will be assumed it is not enabled)
   *
   * @param context the store context
   * @return true if enabled
   */
  public static boolean isDynamicPricingEnabled(@NonNull StoreContext context) {
    Boolean value = (Boolean) ((StoreContextImpl) context)
            .get(AbstractStoreContextProvider.CONFIG_KEY_DYNAMIC_PRICING_ENABLED);
    return value != null ? value : false;
  }

  /**
   * Convenience method to validate the whole context.
   * Checks if all known context values exist.
   *
   * @param context the store context
   * @throws com.coremedia.livecontext.ecommerce.common.InvalidContextException if the context is invalid (missing or wrong typed values)
   */
  public static void validateContext(@NonNull StoreContext context) {
    getStoreId(context);
    getStoreName(context);
    getLocale(context);
    getCurrency(context);
  }

  public static boolean isValid(@NonNull StoreContext storeContext) {
    try {
      validateContext(storeContext);
    } catch (InvalidContextException e) {
      return false;
    }
    return true;
  }

  @NonNull
  private static String formatContext(@NonNull StoreContext context) {
    Map<String, Object> keyValuePairs = ImmutableMap.<String, Object>builder()
            .put("storeId", String.valueOf(context.getStoreId()))
            .put("storeName", String.valueOf(context.getStoreName()))
            .put("catalogId", String.valueOf(context.getCatalogId().map(CatalogId::value).orElse(null)))
            .put("currency", String.valueOf(context.getCurrency()))
            .put("locale", String.valueOf(context.getLocale()))
            .put("workspaceId", context.getWorkspaceId())
            .build();

    return Joiner.on(", ")
            .withKeyValueSeparator(": ")
            .join(keyValuePairs);
  }
}
