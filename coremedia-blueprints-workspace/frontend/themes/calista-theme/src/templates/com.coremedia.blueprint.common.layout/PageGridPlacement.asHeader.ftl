<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.PageGridPlacement" -->

<#assign numberOfItems=self.items?size />
<#assign localizations=cmpage.content.localizations![] />
<#assign cartAction=bp.setting(self,"cartAction", {})/>
<#assign searchAction=bp.setting(self,"shopSearchAction", {})/>

<header id="cm-${self.name!""}" class="cm-header">
  <div class="cm-header__wrapper">
    <#-- mobile hamburger menu -->
    <button type="button"
            class="cm-header__mobile-navigation-button cm-hamburger-icon"
            aria-label="${cm.getMessage("navigation_toggle")}">
      <span class="cm-hamburger-icon__bar1"></span>
      <span class="cm-hamburger-icon__bar2"></span>
      <span class="cm-hamburger-icon__bar3"></span>
    </button>

    <#-- logo -->
    <a class="cm-header__logo cm-logo" href="${cm.getLink(cmpage.navigation.rootNavigation!cm.UNDEFINED)}"></a>

    <#-- search widget -->
    <#if searchAction?has_content>
      <div class="cm-header__mobile-search-button cm-mobile-search-button">
        <span></span>
      </div>
      <div id="cmSearchWrapper" class="cm-header__search cm-search" <@preview.metadata searchAction.content/>>
        <button type="button"
                class="cm-search__form-close cm-hamburger-icon cm-hamburger-icon--toggled"
                aria-label="${cm.getMessage("search_button_close")}">
          <span class="cm-hamburger-icon__bar1"></span>
          <span class="cm-hamburger-icon__bar2"></span>
          <span class="cm-hamburger-icon__bar3"></span>
        </button>
        <#-- substitute to CommerceSearchActionState -->
        <#assign substitution=cm.substitute(searchAction.id!"", searchAction) />
        <@cm.include self=substitution view="asHeader" params={
          "formMethod": "POST"
        }/>
      </div>
    </#if>

    <#-- cart widget -->
    <#if cartAction?has_content>
      <div class="cm-header__cart" <@preview.metadata cartAction.content/>>
        <@cm.include self=cartAction view="asHeader"/>
      </div>
    </#if>

    <ul id="navbar" class="cm-header__navigation">
      <#-- login/logout -->
      <li class="cm-navigation-item cm-navigation-item--depth-1 cm-navigation-item--special-depth-1" data-cm-loginstatus="${lc.getStatusUrl()}">
        <a id="cm-login" class="cm-navigation-item__title" href="${lc.getLoginFormUrl()}" title="${cm.getMessage("login_title")}">${cm.getMessage("login_title")}</a>
        <a id="cm-logout" class="cm-navigation-item__title" href="${lc.getLogoutUrl()}" title="${cm.getMessage("logout_title")}">${cm.getMessage("logout_title")}</a>
      </li>

      <#-- navigation -->
      <li class="cm-header__divider"></li>
      <@cm.include self=cmpage view="navigation"/>
      <li class="cm-header__divider"></li>

      <#-- language/country chooser widget -->
      <#if (localizations?size > 1) >
        <li class="cm-header__language-chooser cm-navigation-item cm-navigation-item--depth-1 cm-navigation-item--special-depth-1">
          <span class="cm-navigation-item__title">${cmpage.locale.displayCountry} (${cmpage.locale.language?upper_case})</span>
          <button type="button" class="cm-navigation-item__toggle" aria-haspopup="true" disabled>
          </button>
          <#list localizations>
            <ul id="localizationMenu" class="cm-navigation-item__menu">
              <#items as localization>
                <#assign variantLink=cm.getLink(localization)!"" />
                <#if localization.locale != cmpage.content.locale && variantLink?has_content>
                  <li class="cm-navigation-item cm-navigation-item--depth-2 cm-navigation-item--special-depth-2">
                    <a href="${variantLink}" class="cm-navigation-item__title">${localization.locale.displayCountry} (${localization.locale.language?upper_case})</a>
                  </li>
                </#if>
              </#items>
            </ul>
          </#list>
        </li>
      </#if>

      <#-- additional header items -->
      <#if (numberOfItems > 0)>
        <#list self.items![] as item>
          <@cm.include self=item view="asLinkListItem" params={
            "cssClass": "cm-navigation-item--special-depth-1",
            "showPicturesInNavigation": false
          } />
        </#list>
      </#if>
    </ul>
  </div>
</header>
