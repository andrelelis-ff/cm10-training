<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMVideo" -->
<#-- @ftlvariable name="blockClass" type="java.lang.String" -->
<#-- @ftlvariable name="cssClasses" type="java.lang.String" -->
<#-- @ftlvariable name="additionalClass" type="java.lang.String" -->
<#-- @ftlvariable name="renderTeaserText" type="java.lang.Boolean" -->
<#-- @ftlvariable name="timelineEntries" type="java.util.List" -->
<#-- @ftlvariable name="metadata" type="java.util.List" -->
<#-- @ftlvariable name="overlay" type="java.util.Map" -->
<#-- @ftlvariable name="entry.link" type="com.coremedia.blueprint.common.contentbeans.CMVideo" -->
<#-- @ftlvariable name="entry.startTimeMillis" type="java.lang.Integer" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#assign hasStillPicture=self.picture?has_content />

<div class="cm-shoppable">
<#-- video on the left -->
  <div class="${blockClass} ${blockClass}--video cm-shoppable__video ${additionalClass}"
       <#if hasStillPicture> data-cm-teasable--video='{"preview": ".cm-teaser__content", "player": ".${blockClass}--video__video", "play": ".cm-play-button", "caption": ".cm-teasable__caption"}'</#if><@preview.metadata (metadata![]) + [self.content] />>
    <div class="${blockClass}__wrapper">
      <#if hasStillPicture>
        <@cm.include self=self.picture!cm.UNDEFINED view="media" params={
          "classBox": "cm-teaser__content cm-shoppable__content",
          "classMedia": "cm-shoppable__still-picture",
          "metadata": ["properties.pictures"]
        }/>
        <#-- play overlay icon-->
        <@cm.include self=self view="_playButton" params={"blockClass": "${blockClass}"}/>
      </#if>

      <@cm.include self=self view="media" params={
        "classBox": "${blockClass}--video__video cm-shoppable__player",
        "classMedia": "cm-shoppable__video-element",
        "hideControls": false,
        "preload": true,
        "autoplay": false,
        "loop": false
      }/>
    </div>
  </div>
  <#-- teaser on the right -->
  <div class="cm-shoppable__teasers"<@preview.metadata "properties.timeLine" />>
    <#-- default teaser -->
    <#if self.timeLineDefaultTarget?has_content>
      <div class="cm-shoppable__teaser cm-shoppable__default">
        <@cm.include self=self.timeLineDefaultTarget!cm.UNDEFINED view="asShoppableTeaser"/>
      </div>
    </#if>
    <#-- list all timeline teaser -->
    <#if (timelineEntries?size > 0)>
      <#list timelineEntries as entry>
        <#if entry.startTimeMillis?has_content && entry.link?has_content>
          <div class="cm-shoppable__teaser" data-cm-video-shoppable-time="${entry.startTimeMillis}">
            <@cm.include self=entry.link view="asShoppableTeaser"/>
          </div>
        </#if>
      </#list>
    </#if>
  </div>
</div>
