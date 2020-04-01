<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMVideo" -->

<#import "*/node_modules/@coremedia/brick-media/src/freemarkerLibs/media.ftl" as media />

<#--
    Template Description:

    This template renders a play button with a link to the video, if "openAsPopup" is set to true. Default is false.
    It overwrites the default play button template from @coremedia/brick-media.

    @since 1907
-->

<#assign blockClass=cm.localParameters().blockClass!"cm-teasable" />
<#assign openAsPopup=cm.localParameters().openAsPopup!false />
<#assign videoLink=media.getLink(self) />
<#assign hideControls = self.playerSettings.hideControls />
<#assign muted = self.playerSettings.muted />
<#assign loop = self.playerSettings.loop />

<#if videoLink?has_content>
  <#if openAsPopup>
    <a href="${cm.getLink(self)}" class="${blockClass}__play cm-play-button cm-button--popup-loading" <@cm.dataAttribute name="data-cm-video-popup" data={ "url": videoLink, "parentSelector": ".${blockClass}", "autoplay": true, "hideControls": hideControls, "muted": muted, "loop": loop } />></a>
  <#else>
    <div class="${blockClass}__play cm-play-button"></div>
  </#if>
</#if>
