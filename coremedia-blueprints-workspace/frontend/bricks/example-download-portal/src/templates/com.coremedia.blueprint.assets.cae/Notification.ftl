<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.cae.Notification" -->
<#-- @ftlvariable name="classBox" type="java.lang.String" -->

<#assign classBox=cm.localParameters().classBox!"" />

<div class="${classBox} am-notification am-notification--${self.type?string?lower_case}">
  <@cm.message key=self.key args=self.params!cm.UNDEFINED highlightErrors=true />
</div>
