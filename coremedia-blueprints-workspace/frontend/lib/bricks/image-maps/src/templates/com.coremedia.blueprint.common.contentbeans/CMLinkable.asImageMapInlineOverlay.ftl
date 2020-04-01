<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMLinkable" -->
<#-- @ftlvariable name="overlay" type="java.util.Map" -->
<#-- @ftlvariable name="classOverlay" type="java.lang.String" -->
<#-- @ftlvariable name="metadata" type="java.util.List" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#-- if overlay configuration is not set explicitly assert false for each key not set -->
<#assign overlay={
  "displayTitle": false
} + overlay!{} />

<#if self.title?has_content && overlay.displayTitle>
  <div class="cm-overlay ${classOverlay}"<@preview.metadata data=(metadata![]) + [self.content] />>
    <@utils.optionalLink href=cm.getLink(self)>
      <div class="cm-overlay__item cm-overlay__item--title"<@preview.metadata "properties.title" />>${self.title}</div>
    </@utils.optionalLink>
  </div>
</#if>
