package com.coremedia.livecontext.ecommerce.ibm.common;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.user.UserContextHelper;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public abstract class AbstractWcWrapperService {

  private static final String PARAM_CATALOG_ID = "catalogId";
  private static final String PARAM_CONTRACT_ID = "contractId";
  private static final String PARAM_LANG_ID = "langId";
  private static final String PARAM_CURRENCY = "currency";
  protected static final String PARAM_FOR_USER = "forUser";
  protected static final String PARAM_FOR_USER_ID = "forUserId";

  private WcRestConnector restConnector;
  private WcLanguageMappingService wcLanguageMappingService;
  private CatalogAliasTranslationService catalogAliasTranslationService;

  @Autowired
  public void setRestConnector(WcRestConnector connector) {
    this.restConnector = connector;
  }

  public WcRestConnector getRestConnector() {
    return restConnector;
  }

  public WcRestConnector getCatalogConnector() {
    return restConnector;
  }

  @Required
  public void setCatalogAliasTranslationService(CatalogAliasTranslationService catalogAliasTranslationService) {
    this.catalogAliasTranslationService = catalogAliasTranslationService;
  }

  protected WcLanguageMappingService getWcLanguageMappingService() {
    return wcLanguageMappingService;
  }

  @Autowired
  void setWcLanguageMappingService(WcLanguageMappingService wcLanguageMappingService) {
    this.wcLanguageMappingService = wcLanguageMappingService;
  }

  protected Map<String, String> getLanguageMapping() {
    return wcLanguageMappingService.getLanguageMapping();
  }

  @NonNull
  protected WcParameterMapBuilder buildParameterMap() {
    return new WcParameterMapBuilder();
  }

  // Deliberately not `final` so that it can be subclassed for
  // extension/customization purposes.
  protected class WcParameterMapBuilder {

    private final Map<String, String[]> parameters;

    private WcParameterMapBuilder() {
      // Tree map keeps keys in order which might help with logging and debugging.
      parameters = new TreeMap<>();
    }

    @NonNull
    public WcParameterMapBuilder withCatalogId(@NonNull CatalogId catalogId) {
      parameters.put(PARAM_CATALOG_ID, new String[]{catalogId.value()});
      return this;
    }

    @NonNull
    public WcParameterMapBuilder withContractIds(@NonNull List<String> contractIds) {
      parameters.put(PARAM_CONTRACT_ID, toArray(contractIds));
      return this;
    }

    @NonNull
    public WcParameterMapBuilder withCurrency(@NonNull StoreContext storeContext) {
      Currency currency = StoreContextHelper.getCurrency(storeContext);
      return withCurrency(currency);
    }

    @NonNull
    public WcParameterMapBuilder withCurrency(@NonNull Currency currency) {
      parameters.put(PARAM_CURRENCY, new String[]{currency.toString()});
      return this;
    }

    @NonNull
    public WcParameterMapBuilder withLanguageId(@NonNull StoreContext storeContext) {
      Locale locale = StoreContextHelper.getLocale(storeContext);
      return withLanguageId(locale);
    }

    @NonNull
    public WcParameterMapBuilder withLanguageId(@NonNull Locale locale) {
      String languageId = getLanguageId(locale);
      return withLanguageId(languageId);
    }

    @NonNull
    public WcParameterMapBuilder withLanguageId(@NonNull String languageId) {
      parameters.put(PARAM_LANG_ID, new String[]{languageId});
      return this;
    }

    @NonNull
    public WcParameterMapBuilder withUserIdOrName(@Nullable UserContext userContext) {
      Integer userId = UserContextHelper.getForUserId(userContext);
      String userName = UserContextHelper.getForUserName(userContext);

      if (userId != null) {
        return withUserId(userId);
      } else if (userName != null) {
        return withUserName(userName);
      } else {
        return this;
      }
    }

    @NonNull
    public WcParameterMapBuilder withUserId(@NonNull Integer userId) {
      parameters.put(PARAM_FOR_USER_ID, new String[]{String.valueOf(userId)});
      return this;
    }

    @NonNull
    public WcParameterMapBuilder withUserName(@NonNull String userName) {
      parameters.put(PARAM_FOR_USER, new String[]{userName});
      return this;
    }

    @NonNull
    public Map<String, String[]> build() {
      // Do not return an unmodifiable map here (at least for now) as some
      // services add parameters on their own (and that makes sense to do).
      //noinspection ReturnOfCollectionOrArrayField
      return parameters;
    }
  }

  /**
   * Gets IBM specific language Id for a given locale String.
   * If a certain mapping does not exist or locale String is invalid, the default "-1" for "en" is returned.
   *
   * @param locale e.g. "en_US" "en" "de"
   */
  @NonNull
  public String getLanguageId(@Nullable Locale locale) {
    return getWcLanguageMappingService().getLanguageId(locale);
  }

  @NonNull
  @VisibleForTesting
  protected Optional<CatalogId> findCatalogId(@NonNull CatalogAlias catalogAlias, @NonNull StoreContext storeContext) {
    String siteId = storeContext.getSiteId();
    return catalogAliasTranslationService.getCatalogIdForAlias(catalogAlias, storeContext);
  }

  @NonNull
  private static String[] toArray(@NonNull List<String> items) {
    // `toArray(new T[0])` as per https://shipilev.net/blog/2016/arrays-wisdom-ancients/
    //noinspection ToArrayCallWithZeroLengthArrayArgument
    return items.toArray(new String[0]);
  }
}
