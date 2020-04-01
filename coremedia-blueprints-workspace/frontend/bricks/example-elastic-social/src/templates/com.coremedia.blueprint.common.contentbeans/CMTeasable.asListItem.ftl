<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTeasable" -->
<#-- @ftlvariable name="viewItem" type="java.lang.String" -->
<#-- @ftlvariable name="metadata" type="java.util.List" -->

<div class="cm-collection__item"<@preview.metadata (metadata![]) + [self.content] />>
  <@cm.include self=self view=viewItem!"asTeaser" />
</div>
