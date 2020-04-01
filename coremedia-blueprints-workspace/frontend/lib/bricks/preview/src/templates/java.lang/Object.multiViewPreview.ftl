<#-- @ftlvariable name="self" type="java.lang.Object" -->
<#-- @ftlvariable name="fragmentViews" type="java.util.List" -->
<#-- @ftlvariable name="additionalAttr" type="java.util.Map" -->
<#-- @ftlvariable name="fragmentView.bean" type="java.lang.Object" -->
<#-- @ftlvariable name="fragmentView.viewName" type="java.lang.String" -->
<#-- @ftlvariable name="fragmentView.viewParams" type="java.util.Map" -->
<#-- @ftlvariable name="fragmentView.titleKey" type="java.lang.String" -->
<#-- @ftlvariable name="fragmentView.title" type="java.lang.String" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<div<@utils.renderAttr attr=additionalAttr!{} /> <@preview.metadata self.content />>
  <#-- iterate over all views as requested by the including template -->
  <#list fragmentViews![] as fragmentView>
    <#assign bean=fragmentView.bean!self />
    <#assign viewName=fragmentView.viewName!cm.UNDEFINED />
    <#assign viewParams=fragmentView.viewParams!{} />
    <#assign titleKey=fragmentView.titleKey!"" />
    <#assign title=fragmentView.title!"" />
    <#-- id may not be generated using bp.generateId, as persisting toggle state in local storage will not work -->
    <#assign toggleId="toggle-" + fragmentView_index + "-" + viewName />

    <div class="toggle-item cm-preview-item" data-id="${toggleId}">
      <a href="#" class="toggle-button cm-preview-item__headline toggle-button--disabled">
        <#if titleKey?has_content && (cm.hasMessage(titleKey) || !title?has_content)>
            <@cm.message titleKey />
          <#else>
        ${title}
        </#if>
      </a>
      <div class="toggle-container cm-preview-item__container">
        <div class="cm-preview-item__content">
          <#if viewName == "DEFAULT"><#-- default = detail view-->
            <@cm.include self=bean params=viewParams />
          <#else>
            <@cm.include self=bean view=viewName params=viewParams />
          </#if>
        </div>
      </div>
    </div>
  </#list>
</div>
