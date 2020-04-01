<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTeasable" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/cta.ftl" as cta />
<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/teaserOverlay.ftl" as teaserOverlay />

<#assign additionalClass=cm.localParameters().additionalClass!"" />
<#assign renderCTA=cm.localParameters().renderCTA!true />

<#assign afterText="">
<#if renderCTA>
  <#assign ctaCls=(self.teaserOverlayStyle.ctaCls)!"" />
  <#assign afterText>
    <@cta.render buttons=self.callToActionSettings
                 additionalClass="cm-teaser-overlay__cta"
                 additionalButtonClass="cm-teaser-overlay__cta-button ${ctaCls}"
                 metadata="properties.targets" />
  </#assign>
</#if>

<@teaserOverlay.render teaserOverlaySettings=self.teaserOverlaySettings
                       teaserOverlayStyle=self.teaserOverlayStyle
                       text=self.content.getMarkup("teaserText")!""
                       additionalClass=additionalClass
                       afterText=afterText />
