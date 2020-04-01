<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMPlaceholder" -->
<#-- @ftlvariable name="cmpage.content" type="com.coremedia.blueprint.ecommerce.contentbeans.CMCategory" -->

<#assign products=cmpage.content.products![]/>
<#assign numberOfItems=products?size />

<#if (numberOfItems > 0)>
  <div class="cm-category"<@preview.metadata self.content />>
    <#-- headline -->
    <h2 class="cm-category__headline"<@preview.metadata "properties.title" />>${self.title}</h2>
    <#-- list of sub-categories -->
    <#if products?has_content>
      <div class="cm-category__products">
        <@cm.include self=bp.getContainer(products) view="asPortraitBanner" />
      </div>
    </#if>
  </div>
</#if>
