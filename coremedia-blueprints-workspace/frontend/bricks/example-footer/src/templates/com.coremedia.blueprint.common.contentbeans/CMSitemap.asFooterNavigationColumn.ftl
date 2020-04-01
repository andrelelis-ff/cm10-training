<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMSitemap" -->

<#if self.teaserTitle?has_content>
  <#assign link=cm.getLink(self!cm.UNDEFINED) />
  <h2 class="cm-footer-navigation-column__title"<@preview.metadata "properties.teaserTitle" />><a href="${link}" class="cm-footer-navigation-column">${self.teaserTitle!""}</a></h2>
</#if>

<#-- we ignore the depth of the sitemap and render just the first level of the navigation tree -->
<#list self.root.visibleChildren![]>
  <ul class="cm-footer-navigation-column"<@preview.metadata "properties.root" />>
    <#items as item>
      <#compress>
        <li class="cm-footer-navigation-column__item">
          <@cm.include self=item view="asFooterNavigationLink" />
        </li>
      </#compress>
    </#items>
  </ul>
</#list>
