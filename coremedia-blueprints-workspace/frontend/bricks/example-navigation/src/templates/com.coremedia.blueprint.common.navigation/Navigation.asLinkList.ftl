<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.navigation.Navigation" -->
<#-- @ftlvariable name="cssClass" type="java.lang.String" -->
<#-- @ftlvariable name="childrenCssClass" type="java.lang.String" -->
<#-- @ftlvariable name="maxDepth" type="java.lang.Integer" -->
<#-- @ftlvariable name="isRoot" type="java.lang.Boolean" -->
<#-- @ftlvariable name="isTopLevel" type="java.lang.Boolean" -->
<#-- @ftlvariable name="showNavigationLabel" type="java.lang.Boolean" -->

<#assign isRoot=(isRoot!true)/>
<#assign cssClass=cm.localParameters().cssClass!""/>
<#assign childrenCssClass=cm.localParameters().childrenCssClass!""/>
<#assign isTopLevel=cm.localParameters().isTopLevel!false/>
<#assign showNavigationLabel=cm.localParameters().showNavigationLabel!false/>
<#assign depth=cm.localParameters().depth!0/>
<#assign showPicturesInNavigation=cm.localParameters().showPicturesInNavigation!true/>

<#-- check if navigation has visible children and max tree depth isn't reached yet -->
<#if self.visibleChildren?has_content && (!(maxDepth?has_content) || (maxDepth > 0))>
  <#-- decrease maxDepth if set-->
  <#if maxDepth?has_content>
    <#assign maxDepth=maxDepth - 1 />
  </#if>

  <#-- list children -->
  <ul class="${cssClass} cm-navigation-item__menu" <#if isRoot><@preview.metadata self.content/></#if>>
    <#if showNavigationLabel>
      <li class="cm-navigation-item__menu-label">
        <@cm.include self=self view="asLink" params={"cssClass": "cm-navigation-item__title"}/>
      </li>
    </#if>
    <#list self.visibleChildren![] as child>
      <@cm.include self=child view="asLinkListItem" params={
        "cssClass": childrenCssClass,
        "isTopLevel": isTopLevel,
        "depth": depth,
        "showPicturesInNavigation": showPicturesInNavigation,
        "showNavigationLabel": isTopLevel
      } />
    </#list>
  </ul>
</#if>
