<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->

<#-- mobile hamburger menu -->
  <button type="button" class="cm-hamburger-icon cm-hamburger-icon--toggled"
          aria-label="${cm.getMessage("navigation_toggle")}">
    <span class="cm-hamburger-icon__bar1"></span>
    <span class="cm-hamburger-icon__bar2"></span>
    <span class="cm-hamburger-icon__bar3"></span>
  </button>

  <ul id="navbar" class="cm-navigation-root">
    <@cm.include self=self view="navigation"/>
  </ul>
