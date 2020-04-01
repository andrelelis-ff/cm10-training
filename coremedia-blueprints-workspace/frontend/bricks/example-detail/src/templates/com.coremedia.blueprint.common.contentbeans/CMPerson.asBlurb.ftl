<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMPerson" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#--
    Template Description:

    This template provides a blurb view for the CMPerson content type. It is used below articles.
    Every author includes an (optional) image, the displayName and an (optional) short biography.

-->

<#assign blockClass=cm.localParameters().blockClass!"cm-author"/>
<#assign shortBioMaxLength=bp.setting(self, "authorShortBiographyMaxLength", 500)/>

<div class="${blockClass}"<@preview.metadata self.content/>>

  <#-- picture on the left with link -->
  <#if self.picture?has_content>
    <a class="${blockClass}__link" href="${cm.getLink(self)?no_esc}">
      <@cm.include self=self.picture view="media" params={
      "classBox": "${blockClass}__picture-box",
      "classMedia": "${blockClass}__picture",
      "metadata": ["properties.picture"]
      }/>
    </a>
  </#if>

  <#-- description on the right -->
  <div class="${blockClass}__description">

    <#-- display name with link -->
    <#if self.displayName?has_content>
      <a class="${blockClass}__link" href="${cm.getLink(self)}">
        <h3 class="${blockClass}__headline"<@preview.metadata self.displayName/>>${self.displayName!""}</h3>
      </a>
    </#if>

    <#-- short bio -->
    <#if self.teaserText?has_content>
      <p class="${blockClass}__short-text"<@preview.metadata self.teaserText/>>
        <@utils.renderWithLineBreaks text=bp.truncateText(self.teaserText!"", shortBioMaxLength)/>
      </p>
    </#if>
  </div>
</div>
