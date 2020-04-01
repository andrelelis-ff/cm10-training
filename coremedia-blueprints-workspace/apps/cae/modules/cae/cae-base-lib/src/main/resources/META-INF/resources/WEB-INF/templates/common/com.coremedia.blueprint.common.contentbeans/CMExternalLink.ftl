<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMExternalLink" -->

<#--
    Template Description:

    Displays an external link with title, image and text.
    Please check example brick "details" for a more detailed version.
-->
<#assign target=self.openInNewTab?then("_blank", "") />
<#assign rel=self.openInNewTab?then("noopener", "") />

<div class="cm-teasable cm-teasable--externallink">
  <#if self.url?has_content>
    <a href="${self.url}" target="${target}" rel="${rel}">
      <h1 class="cm-teasable_title">${self.teaserTitle!""}</h1>
      <div class="cm-teasable__picture">
        <@cm.include self=self.picture!cm.UNDEFINED />
      </div>
      <div class="cm-teasable__text">
        <@cm.include self=self.teaserText!cm.UNDEFINED />
      </div>
    </a>
  </#if>
</div>
