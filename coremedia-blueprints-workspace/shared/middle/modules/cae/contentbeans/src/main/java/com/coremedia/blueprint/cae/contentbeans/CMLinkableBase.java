package com.coremedia.blueprint.cae.contentbeans;


import com.coremedia.blueprint.base.links.UrlPathFormattingHelper;
import com.coremedia.blueprint.base.navigation.context.ContextStrategy;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMLocTaxonomy;
import com.coremedia.blueprint.common.contentbeans.CMLocalized;
import com.coremedia.blueprint.common.contentbeans.CMResourceBundle;
import com.coremedia.blueprint.common.contentbeans.CMSettings;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.blueprint.common.contentbeans.CMViewtype;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.services.validation.ValidationService;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.common.CapStructHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Generated base class for immutable beans of document type CMLinkable.
 * Should not be changed.
 */
public abstract class CMLinkableBase extends CMLocalizedImpl implements CMLinkable {
  static final String IS_IN_PRODUCTION = "isInProduction";

  public static final String ANNOTATED_LINKS_STRUCT_ROOT_PROPERTY_NAME = "links";
  public static final String ANNOTATED_LINK_STRUCT_TARGET_PROPERTY_NAME = "target";

  private ContextStrategy<CMLinkable, CMContext> contextStrategy;

  private ValidationService<Linkable> validationService;

  private SettingsService settingsService;
  private UrlPathFormattingHelper urlPathFormattingHelper;


  // This should be protected, since it is not meant to be a feature of
  // a contentbean, but only for internal usage in subclasses.
  // public only for compatibility reasons.
  public ContextStrategy<CMLinkable, CMContext> getContextStrategy() {
    return contextStrategy;
  }

  @Required
  public void setContextStrategy(ContextStrategy<CMLinkable, CMContext> contextStrategy) {
    if(contextStrategy == null) {
      throw new IllegalArgumentException("supplied 'contextStrategy' must not be null");
    }
    this.contextStrategy = contextStrategy;
  }

  @SuppressWarnings("unchecked")
  public ValidationService<Linkable> getValidationService() {
    return validationService;
  }

  @Required
  public void setValidationService(ValidationService<Linkable> validationService) {
    if(validationService == null) {
      throw new IllegalArgumentException("supplied 'validationService' must not be null");
    }
    this.validationService = validationService;
  }

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link CMLinkable} objects
   */
  @Override
  public CMLinkable getMaster() {
    return (CMLinkable) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMLinkable> getVariantsByLocale() {
    return getVariantsByLocale(CMLinkable.class);
  }

  @Override
  protected <T extends CMLocalized> Map<Locale, T> getVariantsByLocale(Class<T> type) {
    Map<Locale, T> variantsByLocale = super.getVariantsByLocale(type);
    return ImmutableMap.copyOf(Maps.filterValues(variantsByLocale, new Predicate<T>() {
      @Override
      public boolean apply(T variant) {
        return variant instanceof Linkable && validationService.validate((Linkable) variant);
      }
    }));
  }

  public Map<Locale, ? extends CMLinkable> getVariantsByLocaleUnfiltered() {
    return super.getVariantsByLocale(CMLinkable.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMLinkable> getLocalizations() {
    return (Collection<? extends CMLinkable>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMLinkable>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMLinkable>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMLinkable>> getAspects() {
    return (List<? extends Aspect<? extends CMLinkable>>) super.getAspects();
  }

  /**
   * Returns the first value of the document property {@link #VIEWTYPE}.
   *
   * @return a {@link CMViewtype}
   */
  @Override
  public CMViewtype getViewtype() {
    Content viewtype = getContent().getLink(VIEWTYPE);
    return createBeanFor(viewtype, CMViewtype.class);
  }

  /**
   * Returns the value of the document property {@link #KEYWORDS}.
   *
   * @return the value of the document property {@link #KEYWORDS}
   */
  @Override
  public String getKeywords() {
    return getContent().getString(KEYWORDS);
  }

  /**
   * Returns the value of the document property {@link #SEGMENT}.
   *
   * @return the value of the document property {@link #SEGMENT}
   */
  @Override
  public String getSegment() {
    return getContent().getString(SEGMENT);
  }

  /**
   * Returns the value of the document property  {@link #TITLE}
   *
   * @return the value of the document property  {@link #TITLE}
   */
  @Override
  public String getTitle() {
    return getContent().getString(CMLinkable.TITLE);
  }

  /**
   * Returns the value of the document property {@link #HTML_TITLE}
   *
   * @return the value of the document property {@link #HTML_TITLE}
   */
  @Override
  public String getHtmlTitle() {
    String title = getContent().getString(CMLinkable.HTML_TITLE);
    if(StringUtils.isEmpty(title)) {
      title = getContent().getString(CMLinkable.TITLE);
    }
    return title;
  }

  /**
   * Returns the value of the document property {@link #HTML_DESCRIPTION}
   *
   * @return the value of the document property {@link #HTML_DESCRIPTION}
   */
  @Override
  public String getHtmlDescription() {
    return getContent().getString(CMLinkable.HTML_DESCRIPTION);
  }

  /**
   * Returns the value of the document property {@link #LOCAL_SETTINGS}.
   *
   * @return the value of the document property {@link #LOCAL_SETTINGS}
   */
  @Override
  public Struct getLocalSettings() {
    Struct struct = getContent().getStruct(LOCAL_SETTINGS);
    return struct != null ? struct : getContent().getRepository().getConnection().getStructService().emptyStruct();
  }

  /**
   * Returns the value of the document property {@link #LINKED_SETTINGS}.
   *
   * @return a list of {@link CMSettings} objects
   */
  @Override
  public List<CMSettings> getLinkedSettings() {
    List<Content> contents = getContent().getLinks(LINKED_SETTINGS);
    return createBeansFor(contents, CMSettings.class);
  }

  @Override
  public Calendar getValidFrom() {
    return getContent().getDate(CMLinkable.VALID_FROM);
  }

  @Override
  public Calendar getExternallyDisplayedDate() {
    Calendar displayedDate = getContent().getDate(CMLinkable.EXTERNALLY_DISPLAYED_DATE);
    Calendar modificationDate = getContent().getModificationDate();

    if (displayedDate == null) {
      displayedDate = modificationDate;
    }
    return displayedDate;
  }

  @Override
  public Calendar getValidTo() {
    return getContent().getDate(CMLinkable.VALID_TO);
  }

  @Override
  public List<CMTaxonomy> getSubjectTaxonomy() {
    List<Content> contents = getContent().getLinksFulfilling(SUBJECT_TAXONOMY, IS_IN_PRODUCTION);
    return createBeansFor(contents, CMTaxonomy.class);
  }

  @Override
  public List<CMLocTaxonomy> getLocationTaxonomy() {
    List<Content> contents = getContent().getLinksFulfilling(LOCATION_TAXONOMY, IS_IN_PRODUCTION);
    return createBeansFor(contents, CMLocTaxonomy.class);
  }

  @Override
  public List<CMResourceBundle> getResourceBundles2() {
    return createBeansFor(getContent().getLinks(RESOURCE_BUNDLES2), CMResourceBundle.class);
  }

  protected SettingsService getSettingsService() {
    return settingsService;
  }

  protected UrlPathFormattingHelper getUrlPathFormattingHelper() {
    return urlPathFormattingHelper;
  }

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @Required
  public void setUrlPathFormattingHelper(UrlPathFormattingHelper urlPathFormattingHelper) {
    this.urlPathFormattingHelper = urlPathFormattingHelper;
  }

  /**
   * Provide a value for a legacy link property which has been replaced by an annotated link list property.
   *
   * If the annotated link list property holds a value, the first valid target is returned.
   * Otherwise, if the legacy property still holds a value, a corresponding CMLinkable content bean is returned, or null if this
   * bean is invalid according to the configured validation service.
   *
   * @param annotatedLinkListPropertyName the name of the new annotated link list property (e.g. "targets")
   * @param legacyLinkPropertyName the name of the legacy property name (e.g. "target")
   * @return a valid CMLinkable bean, or null
   */
  @Nullable
  protected CMLinkable getLegacyAnnotatedLink(String annotatedLinkListPropertyName, String legacyLinkPropertyName) {
    return getLegacyAnnotatedLinks(annotatedLinkListPropertyName, legacyLinkPropertyName).stream()
      .findFirst()
      .orElse(null);
  }

  /**
   * Provide a value for a legacy link which has been replaced by an annotated link list property.
   *
   * If the annotated link list holds a value, the first valid target is returned.
   * Otherwise, if the legacy link list still holds a value, a corresponding CMLinkable content bean is returned, or null if this
   * bean is invalid according to the configured validation service.
   *
   * @param annotatedLinkList the value of the new annotated link list (e.g. "targets")
   * @param linkList the value of the legacy property (e.g. "target")
   * @return a valid CMLinkable bean, or null
   */
  @Nullable
  protected CMLinkable getLegacyAnnotatedLink(@Nullable Map<String, List<Map<String, Object>>> annotatedLinkList, @Nullable List<CMLinkable> linkList) {
    return getLegacyAnnotatedLinks(annotatedLinkList, linkList).stream()
      .findFirst()
      .orElse(null);
  }

  /**
   * Provide a value for a legacy link list property which has been replaced by an annotated link list property.
   *
   * If the annotated link list property holds a value, the first valid target is returned.
   * Otherwise, if the legacy property still holds a value, a corresponding CMLinkable content bean is returned, or null if this
   * bean is invalid according to the configured validation service.
   *
   * @param annotatedLinkListPropertyName the name of the new annotated link list property (e.g. "targets")
   * @param legacyLinkListPropertyName the name of the legacy property name (e.g. "target")
   * @return a valid CMLinkable bean, or null
   */
  @NonNull
  protected List<CMLinkable> getLegacyAnnotatedLinks(@Nullable String annotatedLinkListPropertyName, @Nullable String legacyLinkListPropertyName) {
    Map<String, List<Map<String, Object>>> filteredAnnotatedLinkList = getAnnotatedLinkList(annotatedLinkListPropertyName, legacyLinkListPropertyName);
    return getAnnotatedLinkListTargets(filteredAnnotatedLinkList);
  }

  /**
   * Provide a value for a legacy link list which has been replaced by an annotated link list property.
   *
   * If the annotated link list holds a value, the first valid target is returned.
   * Otherwise, if the legacy link list still holds a value, a corresponding CMLinkable content bean is returned, or null if this
   * bean is invalid according to the configured validation service.
   *
   * @param annotatedLinkList the value of the new annotated link list (e.g. "targets")
   * @param linkList the value of the legacy property (e.g. "target")
   * @return a valid CMLinkable bean, or null
   */
  @NonNull
  protected List<CMLinkable> getLegacyAnnotatedLinks(@Nullable Map<String, List<Map<String, Object>>> annotatedLinkList, @Nullable List<CMLinkable> linkList) {
    Map<String, List<Map<String, Object>>> filteredAnnotatedLinkList = getAnnotatedLinkList(annotatedLinkList, linkList, null);
    return getAnnotatedLinkListTargets(filteredAnnotatedLinkList);
  }

  /**
   * Get links from a link list without applying any filter.
   *
   * @param linkListPropertyName the property name of the link list or link
   * @return the links of a link list
   */
  @NonNull
  protected List<CMLinkable> getLegacyLinkListUnfiltered(@NonNull String linkListPropertyName) {
    return createBeansFor(CapStructHelper.getLinks(getContent(), linkListPropertyName), CMLinkable.class);
  }

  /**
   * Return the value of an annotated link list property.
   *
   * <p>Annotated link lists are {@link Struct} properties with the following structure:
   * <pre>
   *   {
   *     "links": [
   *       {
   *         "target": target1,
   *         "property1": value1_1,
   *         "property2": "value1_2"
   *       },
   *       {
   *         "target": target2,
   *         "property1": value2_1,
   *         "property2": "value2_2"
   *       },
   *       ...
   *     ]
   *   }
   * </pre>
   *
   * <p>Content references are converted to content beans. Each target is validated against the configured validation service and filtered out if invalid.
   *
   * <p>To help migrating from a plain old link list to a new structured annotated link list, this method accepts a parameter <code>legacyLinkListPropertyName</code>.
   * If the new annotated link list property does not contains a value (yet), the value of this old link list property is taken and converted into the structure above.
   * This makes the new new annotated link list property (which must be a {@link Struct} property) to virtually contain content
   * if only the old link list contains a value.
   *
   * <p>Subclasses may override the {@link #convertLinkListToAnnotatedLinkList} method to populate the converted
   * structure with additional properties beside the target property. Otherwise, each target structure will
   * contain just the target property.
   *
   * @param annotatedLinkListPropertyName the name of the annotated link list struct property
   * @param legacyLinkListPropertyName the name of the plain old link list property, or null.
   *
   * @return a nested map/list object tree according to the structure above
   */
  @NonNull
  protected Map<String, List<Map<String, Object>>> getAnnotatedLinkList(@Nullable String annotatedLinkListPropertyName, @Nullable String legacyLinkListPropertyName) {
    if (annotatedLinkListPropertyName != null) {
      Map<String, List<Map<String, Object>>> annotatedLinkListUnfiltered = getAnnotatedLinkListUnfiltered(annotatedLinkListPropertyName);
      if (!annotatedLinkListUnfiltered.isEmpty()) {
        return filterAnnotatedLinkList(annotatedLinkListUnfiltered);
      }
    }

    if (legacyLinkListPropertyName != null) {
      List<CMLinkable> legacyLinkListUnfiltered = getLegacyLinkListUnfiltered(legacyLinkListPropertyName);
      List<CMLinkable> legacyLinkList = filterLinkList(legacyLinkListUnfiltered);
      List<Map<String, Object>> converted = convertLinkListToAnnotatedLinkList(legacyLinkList, legacyLinkListPropertyName);
      return createLinksStructMap(converted);
    }

    return Collections.emptyMap();
  }

  /**
   * Returns the filtered annotated link list according to the configured validation service.
   *
   * <p>If the annotatedLinkList holds a value, the filtered annotatedLinkList is returned.
   *
   * <p>Otherwise, if the legacyLinkList holds a value, the filtered legacyLinkList is returned.
   *
   * <p>For the structure of the annotated link list, see documentation of {@link #getAnnotatedLinkList(String, String)} for details.
   *
   * <p>Subclasses may override the {@link #convertLinkListToAnnotatedLinkList(List, String)} method to populate the converted
   * structure with additional properties beside the target property. Otherwise, each target structure will
   * contain just the target property. See the implementation of
   * {@link CMTeaserBase#convertLinkListToAnnotatedLinkList} for an example.
   *
   * @param annotatedLinkList the annotatedLinkList
   * @param legacyLinkList the legacy linkList
   * @return the filtered annotated linkList
   */
  @NonNull
  protected Map<String, List<Map<String, Object>>> getAnnotatedLinkList(@Nullable Map<String, List<Map<String, Object>>> annotatedLinkList, @Nullable List<CMLinkable> legacyLinkList, @Nullable String legacyLinkListPropertyName) {
    if (annotatedLinkList != null && !annotatedLinkList.isEmpty()) {
      return filterAnnotatedLinkList(annotatedLinkList);
    }

    if (legacyLinkList != null) {
      List<CMLinkable> filteredLinkList = filterLinkList(legacyLinkList);
      List<Map<String, Object>> converted = convertLinkListToAnnotatedLinkList(filteredLinkList, legacyLinkListPropertyName);
      return createLinksStructMap(converted);
    }

    return Collections.emptyMap();
  }

  /**
   * Get an annotatedLinkList from an annotatedLinkListPropertyName if available, or else an empty map.
   *
   * @param annotatedLinkListPropertyName the name of the annotatedLinkList property
   *
   * @return the annotatedLinkList or an empty map
   */
  @NonNull
  protected Map<String, List<Map<String, Object>>> getAnnotatedLinkListUnfiltered(@NonNull String annotatedLinkListPropertyName) {
    List<Map<String, Object>> linksAsBeans = getAnnotatedLinkListItems(annotatedLinkListPropertyName);
    if (!linksAsBeans.isEmpty()) {
      return createLinksStructMap(linksAsBeans);
    }
    return Collections.emptyMap();
  }

  /**
   * Convert a given link list into the nested structure of an annotated link list without applying any filter.
   *
   * Method can be overridden to add additional functionality.
   *
   * @param linkList the link list
   * @return a list of maps, each map containing a {@link #ANNOTATED_LINK_STRUCT_TARGET_PROPERTY_NAME}
   * property pointing to a CMLinkable bean.
   */
  @NonNull
  protected List<Map<String, Object>> convertLinkListToAnnotatedLinkList(@NonNull List<CMLinkable> linkList, @Nullable String linkListPropertyName) {
    if (linkList.isEmpty()) {
      return Collections.emptyList();
    }
    return IntStream.range(0, linkList.size())
        .mapToObj(i -> createAnnotatedLinkStructMap(linkList.get(i), i + 1, linkListPropertyName))
        .collect(Collectors.toList());
  }

  /**
   * Create annotated link struct map. Method can be overridden to add additional map entries.
   * @param target the target
   * @param index the index of the target
   * @return annotated link struct map
   */
  @NonNull
  protected Map<String, Object> createAnnotatedLinkStructMap(@NonNull CMLinkable target, int index, @Nullable String linkListPropertyName) {
    Map<String, Object> targetStructMap = new LinkedHashMap<>(3);
    targetStructMap.put(ANNOTATED_LINK_STRUCT_TARGET_PROPERTY_NAME, target);
    return targetStructMap;
  }

  /**
   * Get the items of an annotated link list property.
   * @param annotatedLinkListPropertyName the name of the annotatedLinkList property
   * @return the annotated linkList items
   */
  @NonNull
  private List<Map<String, Object>> getAnnotatedLinkListItems(@NonNull String annotatedLinkListPropertyName) {
    Struct targetsValue = CapStructHelper.getStruct(getContent(), annotatedLinkListPropertyName);
    if (targetsValue == null) {
      return Collections.emptyList();
    }
    List<Struct> links = CapStructHelper.getStructs(targetsValue, ANNOTATED_LINKS_STRUCT_ROOT_PROPERTY_NAME);
    return links.stream()
      .map(this::createBeanMapFor)
      .filter(map -> map.get(ANNOTATED_LINK_STRUCT_TARGET_PROPERTY_NAME) != null)
      .collect(Collectors.toList());
  }

  /**
   * Get links from an annotated link list without applying any filter.
   *
   * @param annotatedLinkList the property name of the annotated link list
   * @return the links of an annotated link list
   */
  @NonNull
  private List<CMLinkable> getAnnotatedLinkListTargets(@NonNull Map<String, List<Map<String, Object>>> annotatedLinkList) {
    List<Map<String, Object>> annotatedLinkListItems = getAnnotatedLinkListItems(annotatedLinkList);
    return getLinksFromAnnotatedLinkListItems(annotatedLinkListItems);
  }

  /**
   * Get list of target links from a list of Maps containing the annotated linkList entries
   * @param annotatedLinkListItems the annotated linkList items
   * @return list of target links
   */
  @NonNull
  private List<CMLinkable> getLinksFromAnnotatedLinkListItems(@NonNull List<Map<String, Object>> annotatedLinkListItems) {
    return annotatedLinkListItems.stream()
      .map(entry -> (CMLinkable) entry.get(ANNOTATED_LINK_STRUCT_TARGET_PROPERTY_NAME))
      .collect(Collectors.toList());
  }

  /**
   * Filter the annotated LinkList according to the configured validation service.
   * @param annotatedLinkList the annotated linkList
   * @return the filtered annotated linkList
   */
  @NonNull
  private Map<String, List<Map<String, Object>>> filterAnnotatedLinkList(@NonNull Map<String, List<Map<String, Object>>> annotatedLinkList) {
    List<Map<String, Object>> annotatedLinkListItems = getAnnotatedLinkListItems(annotatedLinkList);
    List<Map<String, Object>> filtered = annotatedLinkListItems.stream()
      .filter(targetMap -> {
        CMLinkable bean = (CMLinkable) targetMap.get(ANNOTATED_LINK_STRUCT_TARGET_PROPERTY_NAME);
        Content content = bean.getContent();
        if (content == null || !content.isInProduction()) {
          return false;
        }
        return getValidationService().validate(bean);
      })
      .collect(Collectors.toList());

    return createLinksStructMap(filtered);
  }

  /**
   * Filter the linkList according to the configured validation service.
   * @param linkList the linkList
   * @return the filtered linkList
   */
  @NonNull
  private  List<CMLinkable> filterLinkList(@NonNull List<CMLinkable> linkList) {
    return linkList.stream()
      .filter(bean -> getValidationService().validate(bean))
      .collect(Collectors.toList());
  }

  /**
   * Get the struct items of an annotated linkList as list.
   * @param annotatedLinkList the annotated linkList
   * @return the struct items of an annotated linkList as list
   */
  private List<Map<String, Object>> getAnnotatedLinkListItems(@NonNull Map<String, List<Map<String, Object>>> annotatedLinkList) {
    return annotatedLinkList.get(CMLinkableBase.ANNOTATED_LINKS_STRUCT_ROOT_PROPERTY_NAME);
  }

  /**
   * Create a "links" struct map (annotated linkList) from the given targetStructMaps
   * @param targetStructMaps the targetStructMaps
   * @return the annotated linkList
   */
  @NonNull
  private static Map<String, List<Map<String, Object>>> createLinksStructMap(@NonNull List<Map<String, Object>> targetStructMaps) {
    return Collections.singletonMap(ANNOTATED_LINKS_STRUCT_ROOT_PROPERTY_NAME, targetStructMaps);
  }
}
  
