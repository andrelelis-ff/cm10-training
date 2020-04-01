<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMMedia" -->
<#assign renderTitle=cm.localParameters().renderTitle!true />
<#assign renderText=cm.localParameters().renderText!true />

<@cm.include self=self view="media" params={
  "classBox": "cm-details__media-box",
  "classMedia": "cm-details__media"
}/>

<#if renderTitle || renderText>
  <div class="cm-details__caption carousel-caption">
    <#-- media title -->
    <#if renderTitle && self.title?has_content>
      <div class="cm-caption__title"<@preview.metadata "properties.title"/>>${self.title!""}</div>
    </#if>
    <#-- media caption -->
    <#if renderText && self.detailText?has_content>
      <div class="cm-caption__text cm-richtext"<@preview.metadata "properties.detailText"/>>
        <@cm.include self=self.detailText!cm.UNDEFINED />
      </div>
    </#if>
  </div>
</#if>
