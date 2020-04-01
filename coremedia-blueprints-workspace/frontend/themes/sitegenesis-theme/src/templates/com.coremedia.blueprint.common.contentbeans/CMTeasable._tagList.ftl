<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTeasable" -->
<#--TODO: Can be deleted after fixing broken tag links-->
<#assign tags=self.subjectTaxonomy![] />

<#if (tags?size > 0)>
  <div class="cm-tag">
    <#-- headline -->
    <h3 class="cm-tag__title"><@cm.message key="tags_label"/></h3>
    <#--tags -->
    <ul class="cm-tag__items">
      <#list tags as tag>
        <li class="cm-tag__item"><span <@preview.metadata data=[tag.content, "properties.teaserTitle"] />>${tag.teaserTitle!""}</span></li>
      </#list>
    </ul>
  </div>
</#if>
