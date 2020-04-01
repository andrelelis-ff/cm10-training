<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMCollection" -->

<#if self.teaserTitle?has_content>
  <h2 class="cm-footer-navigation-column__title"<@preview.metadata "properties.teaserTitle" />>${self.teaserTitle!""}</h2>
</#if>

<#list self.flattenedItems![]>
  <ul class="cm-footer-navigation-column"<@preview.metadata "properties.items" />>
    <#items as item>
      <#compress>
        <li class="cm-footer-navigation-column__item"<@preview.metadata item.content />>
          <@cm.include self=item view="asFooterNavigationLink" />
        </li>
      </#compress>
    </#items>
  </ul>
</#list>
