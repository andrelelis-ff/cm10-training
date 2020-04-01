<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMDownload" -->
<#-- @ftlvariable name="highlightingItem" type="java.util.Map" -->

<#import "../../freemarkerLibs/search.ftl" as search />
<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#--
    Template Description:

    Displays a single search result for a download with an icon, title, text, file type and download size.
    It also includes the viewHook VIEW_HOOK_SEARCH for additional dynamic informations like ratings.
-->

<#assign highlightingItem=cm.localParameters().highlightingItem!{} />
<#assign teaserLength=bp.setting(self, "teaser.max.length", 200)/>
<#assign teaserTitle=(highlightingItem["teaserTitle"][0])!self.teaserTitle!"" />
<#assign teaserText=bp.truncateHighlightedText((highlightingItem["teaserText"][0])!"", teaserLength) />
<#assign htmlDescription=bp.truncateHighlightedText((highlightingItem["htmlDescription"][0])!self.teaserText!"", teaserLength) />
<#assign target=(self.target?has_content && self.target.openInNewTab)?then('target="_blank"', "") />
<#assign rel=(self.target?has_content && self.target.openInNewTab)?then('rel="noopener"', "") />

<div class="cm-search-result__item"<@preview.metadata self.content />>
  <div class="cm-search-result__caption">
    <#-- teaserTitle -->
    <h3 class="cm-search-result__title"<@preview.metadata "properties.teaserTitle" />>
      <a href="${cm.getLink(self.target!cm.UNDEFINED)}" ${target?no_esc} ${rel?no_esc}>
        <i class="cm-search-result__download-icon" aria-hidden="true"></i> ${teaserTitle?no_esc} (${cm.getLink(self)?keep_after_last(".")?keep_before("?") + ", "} ${bp.getDisplaySize(self.data.size)})
      </a>
    </h3>

    <#-- teaserText or htmlDescription as fallback -->
    <#if teaserText?has_content>
      <p<@preview.metadata "properties.teaserText" />>
        <@search.renderDate date=self.externallyDisplayedDate />
        <span<@preview.metadata "properties.teaserText" />><@utils.renderWithLineBreaks text=teaserText /></span>
      </p>
    <#elseif htmlDescription?has_content>
      <p class="cm-search-result__text">
        <@search.renderDate date=self.externallyDisplayedDate />
        <span<@preview.metadata "properties.htmlDescription" />><@utils.renderWithLineBreaks text=htmlDescription /></span>
      </p>
    <#else>
      <p class="cm-search-result__text">
        <@search.renderDate date=self.externallyDisplayedDate />
      </p>
    </#if>

    <#-- add hook for search result items -->
    <@cm.hook id=bp.viewHookEventNames.VIEW_HOOK_SEARCH />
  </div>

</div>
