<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMPicture" -->
<#-- @ftlvariable name="att_class" type="java.lang.String" -->

<#assign additionalCssClasses=att_class!""/>

<div class="cm-richtext-embedded cm-richtext-embedded--image ${additionalCssClasses}">
  <@cm.include self=self view="media" params={
    "classBox": "cm-richtext-embedded__picture-box",
    "classMedia": "cm-richtext-embedded__picture"
  }/>
</div>
