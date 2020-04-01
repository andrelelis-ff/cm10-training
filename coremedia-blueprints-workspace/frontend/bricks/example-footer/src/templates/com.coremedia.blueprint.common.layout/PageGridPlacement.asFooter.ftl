<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.PageGridPlacement" -->

<#assign target='target="_blank"'/>
<#assign rel='rel="noopener"'/>

<#-- This placement is used for the footer section -->
<footer id="cm-${self.name!""}" class="cm-footer"<@preview.metadata data=[bp.getPlacementPropertyName(self), bp.getPlacementHighlightingMetaData(self)!""]/>>
  <div class="cm-footer__wrapper">
    <#-- copyright info in footer (left) -->
    <div class="cm-footer__copyright">${cm.getMessage("copyright")}</div>
    <#-- items of placement as links -->
    <#list self.items![]>
      <ul class="cm-footer__links">
        <#items as link>
          <#compress>
            <li class="cm-footer__item"<@preview.metadata link.content />>
              <@cm.include self=link view="asFooterLink" />
            </li>
          </#compress>
        </#items>
      </ul>
    </#list>
    <div class="cm-footer__social-icons">
      <a href="https://www.facebook.com/coremedia" ${target?no_esc} ${rel?no_esc}><i class="social-icon facebook"></i></a>
      <a href="https://de.linkedin.com/company/coremedia-ag" ${target?no_esc} ${rel?no_esc}><i class="social-icon linkedin"></i></a>
      <a href="https://twitter.com/CoreMedia" ${target?no_esc} ${rel?no_esc}><i class="social-icon twitter"></i></a>
      <a href="https://www.youtube.com/user/coremediachannel" ${target?no_esc} ${rel?no_esc}><i class="social-icon youtube"></i></a>
    </div>
  </div>
</footer>
