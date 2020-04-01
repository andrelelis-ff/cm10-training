<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.Page" -->

<div class="cm-header">
  <#-- mobile hamburger menu -->
  <button type="button" class="cm-header__mobile-navigation-button cm-hamburger-icon" aria-label="${cm.getMessage("navigation_toggle")}">
    <span class="cm-hamburger-icon__bar1"></span>
    <span class="cm-hamburger-icon__bar2"></span>
    <span class="cm-hamburger-icon__bar3"></span>
  </button>

  <ul id="navbar" class="cm-header__navigation">
    <@cm.include self=self view="navigation"/>
  </ul>
</div>
