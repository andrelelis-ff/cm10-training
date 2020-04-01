package com.coremedia.blueprint.common.contentbeans;


import com.coremedia.blueprint.common.datevalidation.ValidityPeriod;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.struct.Struct;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * CMLinkable is the base type for each document which can be referenced
 * as standalone content or navigation unit.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMLinkable}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMLinkable extends Linkable, CMLocalized, BelowRootNavigation, ValidityPeriod {

  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMLinkable'.
   */
  String NAME = "CMLinkable";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a {@link CMLinkable} object
   */
  @Override
  CMLinkable getMaster();

  @Override
  Map<Locale, ? extends CMLinkable> getVariantsByLocale();

  @Override
  Collection<? extends CMLinkable> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMLinkable>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMLinkable>> getAspects();

  /**
   * Name of the document property '  viewtype'.
   */
  String VIEWTYPE = "viewtype";

  /**
   * <p>
   * Returns the first value of the document property {@link #VIEWTYPE}.
   * </p>
   * <dl><dt><strong>Usage:</strong></dt><dd><p>
   * Use given layout as view.
   * </p>
   * <dl>
   * <dt><strong>Freemarker:</strong></dt><dd>
   * <pre>{@code
   * <#assign recursiveInclude=cm.localParameter("recursiveInclude", false) />
   * <#assign layout=(self.viewtype.layout)!"" />
   * <#if layout?has_content && !recursiveInclude>
   *   <@cm.include self=self view="[${layout}]"
   *                params={"recursiveInclude": true} />
   *   <!-- ... --->
   * </#if>
   * }</pre>
   * </dd>
   * </dl>
   * </dd></dl>
   *
   * @return first value of document property of type {@link CMViewtype}
   * @cm.template.api
   */
  CMViewtype getViewtype();

  @Deprecated  // Obsolete with SettingsService
          String LOCAL_SETTINGS = "localSettings";

  /**
   * Name of the document property 'keywords'.
   */
  String KEYWORDS = "keywords";

  /**
   * <p>
   * Returns the value of the document property {@link #KEYWORDS}.
   * </p>
   * <dl><dt><strong>Usage:</strong></dt><dd><p>
   * Add keywords to meta information. {@code self} is of type {@link Page}.
   * </p>
   * <dl>
   * <dt><strong>Freemarker:</strong></dt><dd>
   * <pre>{@code
   * <#if self.content.keywords?has_content>
   *   <meta name="keywords" content="${self.content.keywords}" />
   * </#if>
   * }</pre>
   * </dd>
   * <dt><strong>JSP:</strong></dt><dd>
   * <pre>{@code
   * <c:set var="keywordsList" value="${self.content.keywords}"/>
   * <c:if test="${not empty keywordsList}">
   *   <meta name="keywords" content="<c:out value='${keywordsList}'/>"/>
   * </c:if>
   * }</pre>
   * </dd>
   * </dl>
   * </dd></dl>
   *
   * @return the value of the document property {@link #KEYWORDS}
   * @cm.template.api
   */
  @Override
  String getKeywords();

  /**
   * Name of the document property 'htmlDescription'.
   */
  String HTML_DESCRIPTION = "htmlDescription";


  /**
   * <p>
   * Returns the value of the document property {@link #HTML_DESCRIPTION}.
   * </p>
   * <dl><dt><strong>Usage:</strong></dt><dd><p>
   * Add description to page's meta information in head.
   * </p>
   * <dl>
   * <dt><strong>Freemarker:</strong></dt><dd>
   * <pre>{@code
   * <#if self.htmlDescription?has_content>
   *   <meta name="description" content="${self.htmlDescription}" />
   * </#if>
   * }</pre>
   * </dd>
   * </dl>
   * </dd></dl>
   *
   * @return the value of the document property {@link #HTML_DESCRIPTION}
   * @cm.template.api
   */
  String getHtmlDescription();

  /**
   * Name of the document property 'htmlTitle'.
   */
  String HTML_TITLE = "htmlTitle";

  /**
   * <p>
   * Returns the value of the document property {@link #HTML_TITLE}.
   * </p>
   * <dl><dt><strong>Usage:</strong></dt><dd><p>
   * Add title to meta information in head.
   * </p>
   * <dl>
   * <dt><strong>Freemarker:</strong></dt><dd>
   * <pre>{@code
   * <title>${self.htmlTitle!"CoreMedia CMS - No Page Title"}</title>
   * }</pre>
   * </dd>
   * </dl>
   * </dd></dl>
   *
   * @return the value of the document property {@link #HTML_TITLE}
   * @cm.template.api
   */
  String getHtmlTitle();

  /**
   * Name of the document property 'segment'.
   */
  String SEGMENT = "segment";

  /**
   * Name of the document property 'title'.
   */
  String TITLE = "title";

  /**
   * Returns the title to be used in the head meta data.
   *
   * @return the value of the document property {@link #TITLE}
   * @cm.template.api
   */
  @Override
  String getTitle();

  /**
   * Returns the contexts of this CMLinkable.
   *
   * @return a list of {@link CMContext} objects
   */
  List<CMContext> getContexts();


  /**
   * Return local settings as a Struct.
   *
   * @return local settings. May return null if no settings are found.
   */
  Struct getLocalSettings();

  /**
   * Name of the document property 'linkedSettings'.
   */
  String LINKED_SETTINGS = "linkedSettings";

  /**
   * Returns all {@link CMSettings} linked settings.
   *
   * @return a {@link java.util.List} of {@link CMSettings} objects
   */
  List<CMSettings> getLinkedSettings();

  /**
   * Name of the document property 'validFrom'.
   */
  String VALID_FROM = "validFrom";

  /**
   * Returns the value of the document property {@link #VALID_FROM}.
   *
   * @return the value of the document property {@link #VALID_FROM}
   */
  @Override
  Calendar getValidFrom();

  /**
   * Name of the document property 'validTo'.
   */
  String VALID_TO = "validTo";

  /**
   * Returns the value of the document property {@link #VALID_TO}.
   *
   * @return the value of the document property {@link #VALID_TO}
   */
  @Override
  Calendar getValidTo();

  /**
   * Name of the document property 'externallyDisplayedDate'.
   */
  String EXTERNALLY_DISPLAYED_DATE = "extDisplayedDate";

  /**
   * <p>
   * Returns the value of the document property {@link #EXTERNALLY_DISPLAYED_DATE}.
   * </p>
   * <dl><dt><strong>Usage:</strong></dt><dd><p>
   * Display configured external date if available.
   * </p>
   * <dl>
   * <dt><strong>Freemarker:</strong></dt><dd>
   * <pre>{@code
   * <#if self.externallyDisplayedDate?has_content>
   *   <div class="__date">
   *     <@bp.renderDate self.externallyDisplayedDate.time "__time" />
   *   </div>
   * </#if>
   * }</pre>
   * </dd>
   * </dl>
   * </dd></dl>
   *
   * @return the value of the document property {@link #EXTERNALLY_DISPLAYED_DATE}
   * @cm.template.api
   */
  Calendar getExternallyDisplayedDate();

  /**
   * Name of the document property 'subjectTaxonomy'.
   */
  String SUBJECT_TAXONOMY = "subjectTaxonomy";

  /**
   * <p>
   * Returns the value of the document property {@link #SUBJECT_TAXONOMY}.
   * </p>
   * <dl><dt><strong>Usage:</strong></dt><dd><p>
   * Add links to subject taxonomy overviews.
   * </p>
   * <dl>
   * <dt><strong>Freemarker:</strong></dt><dd>
   * <pre>{@code
   * <#assign tags=self.subjectTaxonomy![] />
   *
   * <#if (tags?size > 0)>
   *   <ul>
   *     <#list tags as taxonomy>
   *       <li>
   *         <@cm.include self=taxonomy view="asLink"/>
   *       </li>
   *     </#list>
   *   </ul>
   * </#if>
   * }</pre>
   * </dd>
   * <dt><strong>JSP:</strong></dt><dd>
   * <pre>{@code
   * <c:set var="subjectTaxonomy" value="${self.subjectTaxonomy}"/>
   * <c:if test="${not empty subjectTaxonomy}">
   *   <ul>
   *     <c:forEach items="${subjectTaxonomy}" var="taxonomy" varStatus="forEachStatus">
   *       <li><cm:include self="${taxonomy}" view="asLink"/></li>
   *     </c:forEach>
   *   </ul>
   * </c:if>
   * }</pre>
   * </dd>
   * </dl>
   * </dd></dl>
   *
   * @return a list of {@link CMTaxonomy} objects
   * @cm.template.api
   */
  List<CMTaxonomy> getSubjectTaxonomy();

  /**
   * Name of the document property 'locationTaxonomy'.
   */
  String LOCATION_TAXONOMY = "locationTaxonomy";

  /**
   * <p>
   * Returns the value of the document property {@link #LOCATION_TAXONOMY}.
   * </p>
   * <dl><dt><strong>Usage:</strong></dt><dd><p>
   * Add links to location taxonomy overviews.
   * </p>
   * <dl>
   * <dt><strong>Freemarker:</strong></dt><dd>
   * <pre>{@code
   * <#assign tags=self.locationTaxonomy![] />
   *
   * <#if (tags?size > 0)>
   *   <ul>
   *     <#list tags as taxonomy>
   *       <li>
   *         <@cm.include self=taxonomy view="asLink"/>
   *       </li>
   *     </#list>
   *   </ul>
   * </#if>
   * }</pre>
   * </dd>
   * <dt><strong>JSP:</strong></dt><dd>
   * <pre>{@code
   * <c:set var="locationTaxonomy" value="${self.locationTaxonomy}"/>
   * <c:if test="${not empty locationTaxonomy}">
   *   <ul>
   *     <c:forEach items="${locationTaxonomy}" var="taxonomy" varStatus="forEachStatus">
   *       <li><cm:include self="${taxonomy}" view="asLink"/></li>
   *     </c:forEach>
   *   </ul>
   * </c:if>
   * }</pre>
   * </dd>
   * </dl>
   * </dd></dl>
   *
   * @return a list of {@link CMLocTaxonomy} objects
   * @cm.template.api
   */
  List<CMLocTaxonomy> getLocationTaxonomy();

  /**
   * Name of the document property 'resourceBundles2'.
   */
  String RESOURCE_BUNDLES2 = "resourceBundles2";

  /**
   * Returns the value of the document property {@link #RESOURCE_BUNDLES2}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMResourceBundle} objects
   */
  List<CMResourceBundle> getResourceBundles2();

  /**
   * Always returns <code>false</code>. This method only serves the purpose to simplify template development. The actual
   * functionality is part of {@link CMExternalLink}.
   *
   * @return always <code>false</code>
   * @cm.template.api
   */
  boolean isOpenInNewTab();
}
