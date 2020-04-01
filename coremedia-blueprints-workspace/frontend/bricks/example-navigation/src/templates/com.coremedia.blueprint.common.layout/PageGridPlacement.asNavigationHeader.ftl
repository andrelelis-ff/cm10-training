<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.PageGridPlacement" -->

<header id="cm-${self.name!""}" class="cm-header cm-header--navigation">
  <#-- mobile hamburger menu -->
  <button type="button" class="cm-header__mobile-navigation-button cm-hamburger-icon"
          aria-label="${cm.getMessage("navigation_toggle")}">
    <span class="cm-hamburger-icon__bar1"></span>
    <span class="cm-hamburger-icon__bar2"></span>
    <span class="cm-hamburger-icon__bar3"></span>
  </button>

  <ul>
    <#-- navigation -->
    <@cm.include self=cmpage view="navigation"/>
  </ul>
</header>
