<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMPlaceholder" -->
<#-- @ftlvariable name="cmpage.content" type="com.coremedia.blueprint.ecommerce.contentbeans.CMCategory" -->

<#assign categories=cmpage.content.subcategories![]/>
<#assign numberOfItems=categories?size />

<#if (numberOfItems > 0)>
  <div class="cm-category"<@preview.metadata self.content />>
    <#-- headline -->
    <h2 class="cm-category__headline"<@preview.metadata "properties.title" />>${self.title!""}</h2>
    <#-- list of sub-categories -->
    <#if categories?has_content>
      <div class="cm-category__subcategories">
        <@cm.include self=bp.getContainer(categories) view="asPortraitBanner" />
      </div>
    </#if>
  </div>
</#if>
