<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMVideo" -->

<#assign hideControls = self.playerSettings.hideControls />
<#if self.playerSettings.autoplay>
  <#assign hideControls = true />
</#if>

<@cm.include self=self view="media" params=cm.localParameters() + {
  "autoplay": self.playerSettings.autoplay,
  "hideControls": hideControls,
  "muted": self.playerSettings.muted,
  "loop": self.playerSettings.autoplay
} />
