<#-- @ftlvariable name="self" type="com.coremedia.livecontext.ecommerce.catalog.Product" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />
<#import "../../freemarkerLibs/defaultTeaser.ftl" as defaultTeaser />

<#assign teaserBlockClass=cm.localParameters().teaserBlockClass!"cm-teasable" />
<#assign renderEmptyImage=cm.localParameters().renderEmptyImage!true />

<div class="${teaserBlockClass}__media">
  <@utils.optionalLink href=defaultTeaser.getLink(self)>
    <#if self.catalogPicture?has_content>
      <@cm.include self=self.catalogPicture!cm.UNDEFINED view="media" params={
        "classBox": "${teaserBlockClass}__picture-box",
        "classMedia": "${teaserBlockClass}__picture",
        <#--player settings for video and audio -->
        "hideControls": true,
        "autoplay": true,
        "loop": true,
        "muted": true,
        "preload": true
      } />
    <#elseif renderEmptyImage>
      <div class="${teaserBlockClass}__picture-box">
          <div class="${teaserBlockClass}__picture"></div>
      </div>
    </#if>
  </@utils.optionalLink>
</div>
