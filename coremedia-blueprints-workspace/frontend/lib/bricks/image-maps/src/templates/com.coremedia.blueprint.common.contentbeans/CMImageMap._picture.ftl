<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMImageMap" -->

<#assign blockClass=cm.localParameters().blockClass!"cm-teasable" />
<#assign renderEmptyImage=cm.localParameter("renderEmptyImage", true) />
<#assign imageMapId=bp.generateId("cm-map-")/>

<#-- display imagemap only if an image exist -->
<#if self.picture?has_content>
  <div class="cm-imagemap__wrapper">
    <#-- include image -->
    <@cm.include self=self.picture view="media" params={
      "classBox": "${blockClass}__picture-box cm-imagemap__picture-box",
      "classMedia":  "${blockClass}__picture cm-imagemap__picture",
      "metadata": ["properties.pictures"],
      "additionalAttr": {"useMap": "#" + imageMapId!"", "unselectable": "on"}
    }/>
    <#-- include map -->
    <@cm.include self=self view="_areasMap" params={"imageMapId": imageMapId}/>
  </div>

<#-- display missing-image placeholder-->
<#elseif renderEmptyImage>
  <div class="${blockClass}__picture-box cm-imagemap__picture-box" <@preview.metadata "properties.pictures" />>
    <div class="${blockClass}__picture cm-imagemap__picture"></div>
  </div>
</#if>
