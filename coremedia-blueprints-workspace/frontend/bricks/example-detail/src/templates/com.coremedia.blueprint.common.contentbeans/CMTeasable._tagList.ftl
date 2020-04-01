<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTeasable" -->

<#--
    Template Description:

    This template renders the property subjectTaxonomy as tag list. each tag links to its taxonomy/topic page.
-->

<#assign parentClass=cm.localParameters().parentClass!"cm-details"/>
<#assign blockClass=cm.localParameters().blockClass!"cm-tag" />
<#assign tags=self.subjectTaxonomy![] />

<#if (tags?size > 0)>
  <section class="${parentClass}__tags ${blockClass}"<@preview.metadata "properties.subjectTaxonomy"/>>
    <#-- headline -->
    <h3 class="${blockClass}__headline"><@cm.message key="tags_label"/></h3>

    <#-- tags -->
    <ul class="${blockClass}__items">
      <#list tags as taxonomy>
        <li class="${blockClass}__item">
          <a href="${cm.getLink(taxonomy.target!cm.UNDEFINED)}"<@preview.metadata data=[taxonomy.content, "properties.teaserTitle"] />>${taxonomy.teaserTitle!""}</a>
        </li>
      </#list>
    </ul>
  </section>
</#if>
