<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMGallery" -->

<#import "../../freemarkerLibs/defaultTeaser.ftl" as defaultTeaser />

<!-- special picture calculation for CMGallery -->
<#assign media=cm.UNDEFINED />
<#assign metadata=[] />
<#if self.firstMedia?has_content>
  <#assign media=self.firstMedia />
  <#assign metadata=["properties.pictures"]/>
<#elseif self.items?has_content>
  <#assign media=self.items[0] />
  <#assign metadata=["properties.items"]/>
</#if>

<#assign teaserBlockClass=cm.localParameters().teaserBlockClass!cm.UNDEFINED />

<div class="${teaserBlockClass}__media">
  <@defaultTeaser.renderMedia media=media
                              mediaView=cm.localParameters().mediaView!cm.UNDEFINED
                              teaserBlockClass=cm.localParameters().teaserBlockClass!cm.UNDEFINED
                              link=defaultTeaser.getLink(self.target!cm.UNDEFINED, self.teaserSettings)
                              openInNewTab=self.openInNewTab
                              metadata=metadata
                              renderEmptyMedia=(self.teaserOverlaySettings.enabled || cm.localParameters().renderEmptyImage!true)
  />
</div>
