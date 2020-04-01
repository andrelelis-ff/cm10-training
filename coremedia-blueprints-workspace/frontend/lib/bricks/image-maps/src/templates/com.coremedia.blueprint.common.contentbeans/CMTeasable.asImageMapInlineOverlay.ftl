<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTeasable" -->
<#-- @ftlvariable name="overlay" type="java.util.Map" -->
<#-- @ftlvariable name="classOverlay" type="java.lang.String" -->
<#-- @ftlvariable name="metadata" type="java.util.List" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#-- if overlay configuration is not set explicitly assert false for each key not set -->
<#assign overlay={
  "displayTitle": false
} + overlay!{} />
<#assign target=(self.target?has_content && self.target.openInNewTab)?then("_blank", "") />
<#assign rel=(self.target?has_content && self.target.openInNewTab)?then("noopener", "") />

<#if overlay.displayTitle && self.teaserTitle?has_content>
  <div class="cm-overlay ${classOverlay}"<@preview.metadata data=(metadata![]) + [self.content] />>
    <@utils.optionalLink href=cm.getLink(self.target!cm.UNDEFINED) attr={"class":"cm-overlay__link", "target":target,"rel":rel}>
      <div class="cm-overlay__item cm-overlay__item--title"<@preview.metadata "properties.teaserTitle" />>${self.teaserTitle}</div>
    </@utils.optionalLink>
  </div>
</#if>
