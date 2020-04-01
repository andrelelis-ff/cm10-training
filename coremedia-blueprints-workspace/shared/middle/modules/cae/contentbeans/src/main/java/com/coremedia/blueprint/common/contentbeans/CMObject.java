package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cae.aspect.provider.AspectsProvider;
import com.coremedia.objectserver.beans.ContentBean;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Root interface for all content beans.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMObject}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMObject extends ContentBean {
  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMObject'.
   */
  String NAME = "CMObject";

  /**
   * <p>
   * Returns the CoreMedia internal id of the underlying {@link com.coremedia.cap.content.Content}.
   * </p>
   * <dl><dt><strong>Usage:</strong></dt><dd><p>
   * Add content id to meta information.
   * </p>
   * <dl>
   * <dt><strong>Freemarker:</strong></dt><dd>
   * <pre>{@code
   * <#if self.contentId?has_content>
   *   <meta name="coremedia_content_id" content="${self.contentId}"/>
   * </#if>
   * }</pre>
   * </dd>
   * <dt><strong>JSP:</strong></dt><dd>
   * <pre>{@code
   * <c:if test="${not empty self.contentId}">
   *   <meta name="coremedia_content_id" content="${self.contentId}"/>
   * </c:if>
   * }</pre>
   * </dd>
   * </dl>
   * </dd></dl>
   *
   * @return the id as int (without any prefix)
   * @cm.template.api
   */
  int getContentId();

  /**
   * <p>
   * Returns a list of all {@link com.coremedia.cae.aspect.Aspect} from all available
   * PlugIns that are registered to this contentbean.
   * </p>
   * <dl><dt><strong>Usage:</strong></dt><dd><p>
   * Add all aspects with view {@code asHead}.
   * </p>
   * <dl>
   * <dt><strong>Freemarker:</strong></dt><dd>
   * <pre>{@code
   * <#list self.aspects as aspect>
   *   <@cm.include self=aspect view="asHeader"/>
   * </#list>
   * }</pre>
   * </dd>
   * <dt><strong>JSP:</strong></dt><dd>
   * <pre>{@code
   * <c:forEach items="${self.aspects}" var="aspect">
   *   <cm:include self="${aspect}" view="asHeader"/>
   * </c:forEach>
   * }</pre>
   * </dd>
   * </dl>
   * </dd></dl>
   *
   * @return a list of Aspects
   *
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  List<? extends Aspect<? extends CMObject>> getAspects();

  /**
   * <p>
   * Returns a Map from aspectIDs to Aspects. AspectIDs consists of an aspectname with a prefix which identifies
   * the plugin provider.
   * </p>
   * <dl><dt><strong>Usage:</strong></dt><dd><p>
   * Access an aspect named <em>osmPlugin</em>.
   * </p>
   * <dl>
   * <dt><strong>Freemarker:</strong></dt><dd>
   * <pre>{@code
   * <#assign osmSocialPlugin=self.aspectByName['osmPlugin'] />
   * }</pre>
   * </dd>
   * <dt><strong>JSP:</strong></dt><dd>
   * <pre>{@code
   * <c:set var="osmSocialPlugin" value="${self.aspectByName['osmPlugin']}"/>
   * }</pre>
   * </dd>
   * </dl>
   * </dd></dl>
   *
   * @return a Map from aspectIDs to Aspects
   *
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  Map<String, ? extends Aspect<? extends CMObject>> getAspectByName();

  /**
   * Provides access to the {@link com.coremedia.cae.aspect.provider.AspectsProvider} which then offers access several {@link Aspect}s for a bean
   *
   * @return the {@link com.coremedia.cae.aspect.provider.AspectsProvider}
   *
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  AspectsProvider getAspectsProvider();
}
