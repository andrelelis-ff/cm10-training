package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.cae.aspect.Aspect;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * Nearly everything except very technical entities is localizable, so this
 * type is nearly top level.
 * </p>
 * <p>
 * Derived doctypes <b>must</b> override the master linklist and restrict it to exactly their own type.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMLocalized}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMLocalized extends CMObject {

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMLocalized'.
   */
  String NAME = "CMLocalized";

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMLocalized>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMLocalized>> getAspects();

  /**
   * Returns the variants of this {@link CMLocalized} indexed by their {@link java.util.Locale}
   *
   * @return the variants of this {@link CMLocalized} indexed by their {@link java.util.Locale}
   */
  Map<Locale, ? extends CMLocalized> getVariantsByLocale();

  /**
   * <p>
   * Returns the {@link java.util.Locale} specific variants of this {@link CMLocalized}
   * </p>
   * <dl><dt><strong>Usage:</strong></dt><dd><p>
   * Add elements for each variant excluding self (self is of type {@link Page} in this example).
   * </p>
   * <dl>
   * <dt><strong>Freemarker:</strong></dt><dd>
   * <pre>{@code
   * <#if (self.content.localizations)?has_content>
   *   <#assign localizations=self.content.localizations![] />
   *   <#list localizations as localization>
   *     <#if localization.locale != self.content.locale>
   *       <link rel="alternate" ... />
   *     </#if>
   *   </#list>
   * </#if>
   * }</pre>
   * </dd>
   * <dt><strong>JSP:</strong></dt><dd>
   * <pre>{@code
   * <c:set var="locales" value="${self.content.localizations}"/>
   * <c:if test="${not empty locales}">
   *   <c:forEach var="localization" items="${locales}">
   *     <c:if test="${localization.locale != self.content.locale}">
   *       <cm:link var="localitationLink" target="${localization}"/>
   *       <link rel="alternate" ... />
   *     </c:if>
   *   </c:forEach>
   * </c:if>
   * }</pre>
   * </dd>
   * </dl>
   * </dd></dl>
   *
   * @return the {@link java.util.Locale} specific variants of this {@link CMLocalized}
   * @cm.template.api
   */
  Collection<? extends CMLocalized> getLocalizations();

  /**
   * Name of the document property 'master'.
   */
  String MASTER = "master";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a {@link CMLocalized} object
   */
  CMLocalized getMaster();

  /**
   * Name of the document property 'masterVersion'.
   */
  String MASTER_VERSION = "masterVersion";

  /**
   * Returns the value of the document property {@link #MASTER_VERSION}.
   *
   * @return the value of the document property {@link #MASTER_VERSION}
   */
  int getMasterVersion();

  /**
   * Name of the document property 'locale'.
   */
  String LOCALE = "locale";

  /**
   * <p>
   * Returns the Locale of this document.
   * </p>
   * <dl><dt><strong>Usage:</strong></dt><dd><p>
   * Given one entry {@code localization}of {@link #getLocalizations()} above, render a link to a alternate locale of the current
   * document.
   * </p>
   * <dl>
   * <dt><strong>Freemarker:</strong></dt><dd>
   * <pre>{@code
   * <link rel="alternate"
   *       hreflang="${localization.locale.toLanguageTag()}"
   *       href="${cm.getLink(localization)}"
   *       title="${localization.locale.getDisplayName(self.content.locale)} | ${localization.locale.getDisplayName()}"/>
   * }</pre>
   * </dd>
   * <dt><strong>JSP:</strong></dt><dd>
   * <pre>{@code
   * <cm:link var="localitationLink" target="${localization}"/>
   * <link rel="alternate"
   *       hreflang="${localization.locale.toLanguageTag()}"
   *       href="${localitationLink}"
   *       title="${localization.locale.getDisplayName(self.content.locale)} | ${localization.locale.getDisplayName()}" />
   * }</pre>
   * </dd>
   * </dl>
   * </dd></dl>
   *
   * @return the Locale of this document.
   * @cm.template.api
   */
  Locale getLocale();

  /**
   * Returns the language of this document which is either the empty string or a lowercase ISO 639 code.
   *
   * @return the language of this document
   */
  String getLang();

  /**
   * Returns the country/region of this document which will either be the empty string or an
   * uppercase ISO 3166 2-letter code.
   *
   * @return the country/region of this document
   */
  String getCountry();

}
