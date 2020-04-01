<#import "utils.ftl" as utils />

<#assign blockClass="cm-teaser-overlay" />

<#function getSettings base={}>
  <#return {
    "enabled": base.enabled!"",
    "positionX": base.positionX!0,
    "positionY": base.positionY!0,
    "width": base.width!0
  } />
</#function>

<#function getStyle style={}>
  <#return {
    "color": style.color!"",
    "backgroundColor": style.backgroundColor!"",
    "cls": style.cls!"",
    "textCls": style.textCls!"",
    "ctaCls": style.ctaCls!"",
    "additionalStyles": style.additionalStyles!""
  } />
</#function>

<#macro render text
               teaserOverlaySettings={}
               teaserOverlayStyle={}
               additionalClass=""
               beforeText=""
               afterText="">
  <#local teaserOverlaySettings=getSettings(teaserOverlaySettings) />
  <#local teaserOverlayStyle=getStyle(teaserOverlayStyle) />

  <#if teaserOverlaySettings.enabled>
    <#local overlayClass=teaserOverlayStyle.cls!"" />
    <#local textClass=teaserOverlayStyle.textCls!"" />
    <#local ctaClass=teaserOverlayStyle.ctaCls!"" />

    <#local overlayStyle=[] />
    <#local positionX=teaserOverlaySettings.positionX + 50 />
    <#local positionY=teaserOverlaySettings.positionY + 50 />
    <#local width=(teaserOverlaySettings.width > 0)?then(teaserOverlaySettings.width, 50) />
    <#local overlayStyle=overlayStyle + ["left: ${positionX}%;"] />
    <#local overlayStyle=overlayStyle + ["margin-right: -${positionX}%;"] />
    <#local overlayStyle=overlayStyle + ["top: ${positionY}%;"] />
    <#local overlayStyle=overlayStyle + ["margin-bottom: -${positionY}%;"] />
    <#local overlayStyle=overlayStyle + ["transform: translate(-${positionX}%, -${positionY}%);"] />
    <#local overlayStyle=overlayStyle + ["width: ${width}%;"] />

    <#if teaserOverlayStyle.color?has_content>
      <#local overlayStyle=overlayStyle + ["color: ${teaserOverlayStyle.color};"] />
    </#if>
    <#if teaserOverlayStyle.backgroundColor?has_content>
      <#local overlayStyle=overlayStyle + ["background-color: ${teaserOverlayStyle.backgroundColor};"] />
    </#if>
    <#if teaserOverlayStyle.additionalStyles?has_content>
      <#local overlayStyle=overlayStyle + [teaserOverlayStyle.additionalStyles] />
    </#if>

    <div class="${blockClass} ${additionalClass} ${overlayClass}"<@utils.optionalAttributes {"style": overlayStyle?join(" ")} />>
      ${beforeText}
      <div class="${blockClass}__text cm-richtext ${textClass}"<@preview.metadata ["properties.teaserText"] />>
        <#if text?has_content>
          <@cm.include self=text />
        </#if>
      </div>
      ${afterText}
    </div>
  </#if>
</#macro>
