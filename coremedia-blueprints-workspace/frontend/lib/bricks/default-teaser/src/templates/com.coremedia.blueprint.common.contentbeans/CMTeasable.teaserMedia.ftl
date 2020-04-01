<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTeasable" -->

<#--
  Template Description:

  @param inlineMedia specifies which media to display inline. if not set, the banner will display its firstMedia property
  @param renderInlineMediaOnly specifies what to render if inlineMedia is undefined. If true, the banner will render no fallback, otherwise firstMedia will be rendered. Should only be set if inlineMedia is set as well.
-->

<#import "../../freemarkerLibs/defaultTeaser.ftl" as defaultTeaser />

<#assign teaserBlockClass=cm.localParameters().teaserBlockClass!cm.UNDEFINED />
<#assign inlineMedia=cm.localParameters().inlineMedia!cm.UNDEFINED />
<#assign renderInlineMediaOnly=cm.localParameters().renderInlineMediaOnly!false />

<div class="${teaserBlockClass}__media">
  <@defaultTeaser.renderMedia media=(renderInlineMediaOnly || !cm.isUndefined(inlineMedia))?then(inlineMedia, self.firstMedia!cm.UNDEFINED)
    mediaView=cm.localParameters().mediaView!cm.UNDEFINED
    teaserBlockClass=teaserBlockClass
    link=defaultTeaser.getLink(self.target!cm.UNDEFINED, self.teaserSettings)
    openInNewTab=self.openInNewTab
    metadata=["properties.pictures"]
    renderEmptyMedia=(self.teaserOverlaySettings.enabled || cm.localParameters().renderEmptyImage!true)
  />

  <#-- do not render a video popup if no link to detail page should be rendered (popup is a replacement for the detail page) -->
  <#if self.teaserSettings.renderLinkToDetailPage && !cm.localParameters().renderVideoInline!false>
    <@cm.include self=self.target!cm.UNDEFINED view="_playButton"
      params=cm.isUndefined(teaserBlockClass)?then({}, {"blockClass": teaserBlockClass}) + {"openAsPopup": true}
    />
  </#if>
</div>
