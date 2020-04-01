<#-- @ftlvariable name="self" type="com.coremedia.livecontext.search.CommerceSearchActionState" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />

<#assign formMethod=cm.localParameter("formMethod", "GET") />
<#assign minSearchLength=3 />

<form class="cm-search--form" method="${formMethod}" action="${cm.getLink(self)}" autocomplete="off">
  <fieldset class="cm-search__form-fieldset">
    <label for="SimpleSearchForm_SearchTerm" class="cm-search__form-label" <@preview.metadata "properties.title" />>${self.action.title!""}</label>
    <#-- id and class is used by wcs -->
    <input id="SimpleSearchForm_SearchTerm" type="search" class="cm-search__form-input" placeholder="${cm.getMessage("search_placeholder")}" name="query" value=""<@preview.metadata "properties.title" /> required>
  </fieldset>
  <button type="submit" class="cm-search__form-button" title="${self.action.title!""}"<@preview.metadata "properties.title" />>
</form>
