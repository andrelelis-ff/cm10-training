<#-- @ftlvariable name="self" type="com.coremedia.livecontext.contentbeans.LiveContextExternalChannel" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />
<#import "../../freemarkerLibs/defaultTeaser.ftl" as defaultTeaser />

<#assign teaserBlockClass=cm.localParameters().teaserBlockClass!cm.UNDEFINED />

<div class="${teaserBlockClass}__media">
  <@utils.optionalLink href=defaultTeaser.getLink(self.target!cm.UNDEFINED, self.teaserSettings)
                       openInNewTab=self.openInNewTab>
    <@cm.include self=self view="_picture" params={
      "blockClass": cm.localParameters().teaserBlockClass!"cm-teasable",
      "renderEmptyImage": cm.localParameters().renderEmptyImage!true
    }/>
  </@utils.optionalLink>
</div>
