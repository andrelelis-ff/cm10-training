<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMImageMap" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />
<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />

<#--
  Template Description:

  This template extends @coremedia/brick-image-maps and adds popups for the targets of the areas.
  For the popup layout, check the template *.asPopup.ftl.

  @since 1907
-->

<#-- if imageMapId is not given, generate new id -->
<#assign imageMapId=cm.localParameters().imageMapId!(bp.generateId("cm-map-")) />
<#assign imageMapAreas=bp.responsiveImageMapAreas(self) />
<#assign areaCssClass="cm-imagemap__area"/>
<#assign popupIndex=0 />

<#--imagemap with areas -->
<map <@utils.renderAttr attr={ "name": imageMapId, "classes": ["cm-imagemap__areas"], "data-cm-imagemap-popup": "" } /> <@preview.metadata "properties.imageMapAreas" />>
  <#-- show hotzones as areas with inline overlay or as icon -->
  <#list imageMapAreas![] as imageMapArea>
    <#assign dataCoords=bp.responsiveImageMapAreaData(imageMapArea.coords)/>
    <#if imageMapArea.linkedContent?has_content>
      <#assign linkedContent=imageMapArea.linkedContent />
      <#assign link=cm.getLink(linkedContent.target!cm.UNDEFINED) />
      <#assign popupId=bp.generateId("cm-popup-") />

      <#-- inline overlay without popup -->
      <#if imageMapArea.displayAsInlineOverlay!false>
        <#assign classOverlay="" />
        <#assign theme=imageMapArea.inlineOverlayTheme!"" />
        <#-- only allow valid themes -->
        <#if (["dark", "light", "dark-on-light", "light-on-dark"]?seq_contains(theme))>
          <#assign classOverlay="cm-overlay--theme-" + theme />
        </#if>
        <#-- hot zones as areas -->
        <area shape="${imageMapArea.shape}"
              coords="0,0,0,0"
              <@cm.dataAttribute name="data-coords" data=dataCoords />
              href="${link}"
              class="${areaCssClass}"
              alt="${imageMapArea.alt!""}"
        >
        <#-- overlay -->
        <div class="cm-imagemap__hotzone cm-imagemap__hotzone--text">
          <@cm.include self=linkedContent!cm.UNDEFINED view="asImageMapInlineOverlay" params={
            "classOverlay": classOverlay,
            "overlay": bp.setting(self, "overlay", {})
          } />
        </div>

      <#-- icon with popup -->
      <#else>
        <#assign parameters={
          "data-cm-imagemap-target": "#${popupId}",
          "data-cm-imagemap-target-id": "${popupIndex}",
          "class": "cm-imagemap__hotzone cm-imagemap__hotzone--icon cm-imagemap__hotzone--loading",
          "metadata": linkedContent.content
        }/>
        <#-- hot zones as areas -->
        <area shape="${imageMapArea.shape}"
              coords="0,0,0,0"
              <@cm.dataAttribute name="data-coords" data=dataCoords />
              href="${link}"
              class="${areaCssClass}"
              alt="${imageMapArea.alt!""}"
              data-cm-imagemap-popup-target-id="${popupIndex}"
        >
        <#-- icon -->
        <@components.button baseClass="" href="${link}" iconText=(linkedContent.teaserTitle!cm.getMessage("button_popup")) attr=parameters />
        <#-- popup -->
        <@cm.include self=linkedContent view="asPopup" params={
          "popupId" : "${popupId}",
          "additionalClass": "mfp-hide",
          "overlay": bp.setting(self, "overlay", {})
        } />
        <#assign popupIndex=popupIndex+1 />
      </#if>
    </#if>

  <#-- add area for default target if no hotzones are available -->
  <#else>
    <#assign target=(self.target?has_content && self.target.openInNewTab)?then('target="_blank"', "") />
    <#assign rel=(self.target?has_content && self.target.openInNewTab)?then('rel="noopener"', "") />
    <area shape="default" href="${cm.getLink(self.target!cm.UNDEFINED)}" ${target?no_esc} ${rel?no_esc} class="cm-imagemap__area" alt="">
  </#list>
</map>
