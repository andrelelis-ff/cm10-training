<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/cta.ftl" as cta />
<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />
<#import "*/node_modules/@coremedia/brick-media/src/freemarkerLibs/media.ftl" as mediaLib />

<#assign defaultBlockClass="cm-teasable" />

<#function getLink target teaserSettings={}>
  <#-- @ftlvariable name="teaserSettings" type="com.coremedia.blueprint.common.teaser.TeaserSettings" -->
  <#return (cm.localParameters().renderLink!teaserSettings.renderLinkToDetailPage!true)?then(cm.getLink(target!cm.UNDEFINED), "") />
</#function>

<#--
  Renders the media of a teaser.

  @param media a CMMedia contentbean to retrieve data from
  @param mediaView the view to render the media
  @param teaserBlockClass the teaser block class to use
  @param additionalClass an additional class to attach to the root element
  @param link if specified wraps the media into a link tag with the given link as href
  @param openInNewTab specifies if the given link shall be opened in a new tab
  @param metadata preview metadata to attach to the root element
  @param renderEmptyMedia specifies if an image placeholder should be rendered if the given media is empty

  Example:
  <@renderMedia media=self.picture additionalClass="some-picture" />
-->
<#macro renderMedia media
                    mediaView="media"
                    teaserBlockClass=defaultBlockClass
                    additionalClass=""
                    link=""
                    openInNewTab=false
                    metadata=[]
                    renderEmptyMedia=true>
  <#-- @ftlvariable name="media" type="com.coremedia.blueprint.common.contentbeans.CMMedia" -->

  <#--
   Provide fallback values as long as "cm.localParameter" is used to fill the parameters of the macro.
   Can be removed as soon as this is not supported anymore.

   Important: These values need to be kept in sync with the signature of the macro
   -->
  <#local mediaView=cm.notUndefined(mediaView, "media") />
  <#local teaserBlockClass=cm.notUndefined(teaserBlockClass, defaultBlockClass) />
  <#local additionalClass=cm.notUndefined(additionalClass, "") />
  <#local link=cm.notUndefined(link, "") />
  <#local openInNewTab=cm.notUndefined(openInNewTab, false) />
  <#local metadata=cm.notUndefined(metadata, []) />
  <#local renderEmptyMedia=cm.notUndefined(renderEmptyMedia, true) />

  <@utils.optionalLink href="${link}" openInNewTab=openInNewTab attr={"class": "${teaserBlockClass}__link"}>
    <#if media?has_content && !cm.isUndefined(media)>
      <#-- media -->
      <@cm.include self=media view=mediaView params={
        "classBox": "${teaserBlockClass}__picture-box",
        "classMedia": "${teaserBlockClass}__picture",
        "metadata": metadata,
        <#--player settings for video and audio -->
        "hideControls": true,
        "autoplay": true,
        "loop": true,
        "muted": true,
        "preload": true
      }/>
    <#else>
      <#if renderEmptyMedia>
        <div class="${teaserBlockClass}__picture-box ${teaserBlockClass}__empty-picture-box"<@preview.metadata metadata />>
          <@mediaLib.renderEmptyMedia additionalClass="${teaserBlockClass}__picture" />
        </div>
      </#if>
    </#if>
  </@utils.optionalLink>
</#macro>

<#--
  Renders the caption of a teaser.

  @param title if specified a title element is inserted into the caption with the given content
  @param text if specified a text element is inserted into the caption with the given content
  @param additional if specified an additional element is inserted into the caption with the given content
  @param link if specified adds a link to the title element of the caption
  @param openInNewTab specifies if the given link shall be opened in a new tab
  @param ctaButtons a list of CallToActionButtonSettings to use
  @param teaserBlockClass the teaser block class to use
  @param metadataTitle preview metadata to attach to the title element
  @param metadataText preview metadata to attach to the text element
  @param authors a list of authors, linked to the given content
  @param externallyDisplayedDate the date to be displayed for the given content

  Example:
  <@renderCaption title=self.title />
-->
<#macro renderCaption title=""
                      text=""
                      additional=""
                      link=""
                      openInNewTab=false
                      ctaButtons=[]
                      teaserBlockClass=defaultBlockClass
                      externallyDisplayedDate=[]
                      authors=[]
                      metadataTitle=[]
                      metadataText=[]>
  <#--
  Provide fallback values as long as "cm.localParameter" is used to fill the parameters of the macro.
  Can be removed as soon as this is not supported anymore.

  Important: These values need to be kept in sync with the signature of the macro
  -->
  <#local link=cm.notUndefined(link, "") />
  <#local openInNewTab=cm.notUndefined(openInNewTab, false) />
  <#local teaserBlockClass=cm.notUndefined(teaserBlockClass, defaultBlockClass) />
  <#local metadataTitle=cm.notUndefined(metadataTitle, []) />
  <#local metadataText=cm.notUndefined(metadataText, []) />

  <div class="${teaserBlockClass}__caption">

    <#if externallyDisplayedDate?has_content || authors?has_content>
    <p class="${teaserBlockClass}__editorial" <@preview.metadata "properties.authors"/>>
      <#if externallyDisplayedDate?has_content>
        <#-- date -->
        <span class="${teaserBlockClass}__time">
          <@utils.renderDate date=externallyDisplayedDate.time/>
        </span>
      </#if>

      <#if authors?has_content>
        <#-- authors -->
        <#list authors![] as author>
        <span>
          <a href="${cm.getLink(author)}" class="${teaserBlockClass}__author" <@preview.metadata author.content/>>${author.displayName!""}</a>
        </span>
        </#list>
      </#if>
    </p>
    </#if>

    <#-- title -->
    <#if title?has_content>
      <@utils.optionalLink attr={"class":"${teaserBlockClass}__title"} href=link openInNewTab=openInNewTab>
        <h3 class="${teaserBlockClass}__headline"<@preview.metadata metadataTitle />>${title}</h3>
      </@utils.optionalLink>
    </#if>

    <#-- teaser text -->
    <#if text?has_content>
      <div class="${teaserBlockClass}__text"<@preview.metadata metadataText />>
        ${text}
      </div>
    </#if>

    <#-- teaser additional elements -->
    <#if additional?has_content>
      <div class="${teaserBlockClass}__additional">
        ${additional}
      </div>
    </#if>

    <#-- cta -->
    <@cta.render buttons=ctaButtons
                 additionalClass="${teaserBlockClass}__cta" />
  </div>
</#macro>
