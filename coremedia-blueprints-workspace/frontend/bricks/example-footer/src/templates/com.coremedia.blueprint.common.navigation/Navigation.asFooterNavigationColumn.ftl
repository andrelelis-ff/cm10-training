<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.navigation.Navigation" -->

<#if self.title?has_content>
  <#assign link=cm.getLink(self!cm.UNDEFINED) />
  <h2 class="cm-footer-navigation-column__title"><a href="${link}" class="cm-footer-navigation-column__link">${self.title!""}</a></h2>
</#if>

<#-- we ignore the depth of the sitemap and render just the first level of the navigation tree -->
<#list self.visibleChildren![]>
  <ul class="cm-footer-navigation-column">
    <#items as item>
      <#compress>
        <li class="cm-footer-navigation-column__item">
          <@cm.include self=item view="asFooterNavigationLink" />
        </li>
      </#compress>
    </#items>
  </ul>
</#list>
