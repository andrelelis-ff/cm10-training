<#-- @ftlvariable name="self" type="com.coremedia.blueprint.cae.action.search.SearchActionState" -->
<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />

<#--
    Template Description:

    Displays a simple search form including a label, an input field and a submit button with an optional icon and label.
    The input field uses the new html5 type "search", minlength and required.
-->

<#assign searchQuery=self.form.query!""/>
<#assign searchLink=cm.getLink(self!cm.UNDEFINED, {"page": cmpage})/>
<#assign additionalCssClass=searchQuery?has_content?then(" focus", "")/>

<form class="cm-search--form" action="${searchLink}" autocomplete="off" role="search">
  <fieldset class="cm-search__form-fieldset">
    <label for="cm-search-query" class="cm-search__form-label">${cm.getMessage("search_label")}</label>
    <input id="cm-search-query" type="search" class="cm-search__form-input${additionalCssClass}" name="query" value="${searchQuery}" placeholder="${cm.getMessage("search_placeholder")}" minlength="${self.minimalSearchQueryLength!3}" required/>
  </fieldset>
  <@components.button text=cm.getMessage("search_button_label") baseClass="" attr={"type": "submit", "class": "cm-search__form-button", "title":  self.action.title!"", "metadata": "properties.title"} />
</form>
