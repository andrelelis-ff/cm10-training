<#-- @ftlvariable name="self" type="com.coremedia.livecontext.ecommerce.catalog.Category" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />
<#import "../../freemarkerLibs/defaultTeaser.ftl" as defaultTeaser />

<#assign renderTeaserText=cm.localParameters().renderTeaserText!true />
<#assign textHtml>
  <#if renderTeaserText && self.shortDescription?has_content>
    <@utils.renderWithLineBreaks text=bp.truncateText(self.shortDescription, bp.setting(self, "teaser.max.length", 200)) />
  </#if>
</#assign>

<#assign renderCTA=cm.localParameters().renderCTA!true /> <#-- de-/activate CTAs generally-->

<@defaultTeaser.renderCaption title=(cm.localParameters().renderTeaserTitle!true)?then(self.name!"", "")
                              text=textHtml?no_esc
                              link=defaultTeaser.getLink(self)
                              teaserBlockClass=cm.localParameters().teaserBlockClass!cm.UNDEFINED/>
