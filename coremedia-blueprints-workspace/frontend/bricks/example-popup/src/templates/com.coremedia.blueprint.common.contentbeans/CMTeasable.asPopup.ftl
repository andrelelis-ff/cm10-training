<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTeasable" -->
<#-- @ftlvariable name="overlay" type="java.util.Map" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#--
  Template Description:

  This template renders the layout "asPopup" for all CMTeasables with an image, title, text and CTA.
  For products, check LiveContextProductTeasable.asPopup.ftl

  @since 1907
-->

<#-- if overlay configuration is not set explicitly assert false for each key not set -->
<#assign popupId=cm.localParameters().popupId?has_content?then('id="' + cm.localParameters().popupId + '"', "") />
<#assign additionalClass=cm.localParameters().additionalClass!"" />
<#assign overlay={
  "displayTitle": false,
  "displayShortText": false,
  "displayPicture": false,
  "displayDefaultPrice": false,
  "displayDiscountedPrice": false,
  "displayOutOfStockLink": false
} + cm.localParameters().overlay!{} />
<#assign showTitle=self.teaserTitle?has_content && overlay.displayTitle />
<#assign showTeaserText=self.teaserText?has_content && overlay.displayShortText />

<div ${popupId?no_esc} class="cm-popup ${additionalClass}" <@preview.metadata self.content />>
  <#-- image -->
  <#if overlay.displayPicture>
    <div class="cm-popup__container">
      <a href="${cm.getLink(self.target!(cm.UNDEFINED))}" class="cm-popup__picture-link">
        <#assign pictureParams={
          "classBox": "cm-popup__picture-box",
          "classMedia": "cm-popup__picture",
          "metadata": ["properties.pictures"]
        } />
        <#if self.picture?has_content>
          <@cm.include self=self.picture!cm.UNDEFINED view="media" params=pictureParams />
        </#if>
      </a>
    </div>
  </#if>
  <div class="cm-popup__container">
    <div class="cm-popup__content">
      <#-- teaserTitle -->
      <div class="cm-popup__header">
        <#if showTitle>
          <h5 class="cm-popup__title"<@preview.metadata "properties.teaserTitle" />>${self.teaserTitle}</h5>
        </#if>
      </div>
      <#-- teaserText -->
      <div class="cm-popup__text"<@preview.metadata "properties.teaserText" />>
        <#if showTeaserText>
          <@utils.renderWithLineBreaks text=bp.truncateText(self.teaserText!"", 175) />
        </#if>
      </div>
      <#-- call to action button -->
      <@cm.include self=self view="_popupButton" />
    </div>
  </div>
</div>
