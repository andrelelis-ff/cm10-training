<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.cae.exception.ContentError" -->
<@bp.errorpage title="Content Error" message=(self.message)!"">
  <#if preview.isPreviewCae() && self.wrappedException?has_content>
    <!-- stacktrace
      <ul>
        <#list self.wrappedException.stackTrace![] as message>
          <li>${message!""}</li>
        </#list>
      </ul>
    -->
  </#if>
</@bp.errorpage>
