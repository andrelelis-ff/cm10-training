<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/>
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->
<#-- @ftlvariable name="cssClass" type="java.lang.String" -->
<#-- @ftlvariable name="childrenCssClass" type="java.lang.String" -->

<#assign cssClass=cm.localParameters().cssClass!""/>
<#assign childrenCssClass=cm.localParameters().childrenCssClass!""/>
<#assign showPicturesInNavigation=cm.localParameters().showPicturesInNavigation!true/>

<#if (cmpage.navigation.rootNavigation)?has_content>
  <li id="cm-navigation" class="cm-navigation">
    <@cm.include self=self.navigation.rootNavigation view="asLinkList" params={
    "maxDepth": bp.setting(self, "navigation_depth", 3),
    "isTopLevel": true,
    "depth" : 0,
    "cssClass": cssClass,
    "showPicturesInNavigation": showPicturesInNavigation,
    "childrenCssClass": childrenCssClass
    } />
  </li>
</#if>
