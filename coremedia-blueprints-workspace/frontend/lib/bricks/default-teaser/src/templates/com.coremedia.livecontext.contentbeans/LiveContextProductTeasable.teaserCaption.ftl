<#-- @ftlvariable name="self" type="com.coremedia.livecontext.contentbeans.LiveContextProductTeasable" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />
<#import "../../freemarkerLibs/defaultTeaser.ftl" as defaultTeaser />

<#--
  Template description:
  Special variant for "teaserCaption" rendering the price of the product and the Shop-Now Button.
  Keep in sync with CMTeasable.teaserCaption.ftl
-->

<#assign teaserBlockClass=cm.localParameters().teaserBlockClass!"" />
<#assign renderTeaserText=cm.localParameters().renderTeaserText!true />
<#assign textHtml>
  <#if renderTeaserText && self.teaserText?has_content>
    <@utils.renderWithLineBreaks text=bp.truncateText(self.teaserText!"", bp.setting(self, "teaser.max.length", 200)) />
  </#if>
</#assign>
<#assign additionalHtml>
  <@cm.include self=self.product!cm.UNDEFINED view="pricing"/>
</#assign>
<#assign renderCTA=cm.localParameters().renderCTA!true /> <#-- de-/activate CTAs generally-->

<@defaultTeaser.renderCaption title=(cm.localParameters().renderTeaserTitle!true)?then(self.teaserTitle!"", "")
                              text=textHtml?no_esc
                              additional=additionalHtml?no_esc
                              link=defaultTeaser.getLink(self.target!cm.UNDEFINED, self.teaserSettings)
                              openInNewTab=self.openInNewTab
                              ctaButtons=renderCTA?then(self.callToActionSettings, [])
                              teaserBlockClass=teaserBlockClass
                              authors=(cm.localParameters().renderAuthors!false)?then(self.authors![], [])
                              externallyDisplayedDate=(cm.localParameters().renderDate!false)?then(self.externallyDisplayedDate![], [])
                              metadataTitle=["properties.teaserTitle"]
                              metadataText=["properties.teaserText"] />

<#if self.isShopNowEnabled(cmpage.context)>
  <@cm.include self=self view="_shopNow" params={"teaserBlockClass": teaserBlockClass} />
</#if>
