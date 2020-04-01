<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/utils.ftl" as utils />

<#assign defaultBlockClass="cm-media" />

<#--
  Returns the link to the video of a given CMMedia.

  @param media a CMMedia instance to retrieve the video link from

  Example:
  <#assign link = bp.getLink(self) />
-->
<#function getLink media>
  <#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMMedia" -->
  <#local videoLink=media.dataUrl!"" />
  <#if !videoLink?has_content && media.data?has_content>
    <#local videoLink=cm.getLink(media.data)!"" />
  </#if>
  <#return videoLink />
</#function>

<#macro renderPicture blockClass=defaultBlockClass
                      asBackground=false
                      additionalClass=""
                      src=""
                      alt=""
                      title=""
                      enableRetina=false
                      responsiveData=""
                      metadata=[]
                      additionalAttributes={}>
  <#-- make sure we are working on a copy of the attributes -->
  <#local additionalAttributes = {} + additionalAttributes!{} />
  <#local additionalAttributes += {"title": title} />
  <#if enableRetina>
    <#local additionalAttributes += {"data-cm-retina": "true"} />
  </#if>
  <#if responsiveData?has_content>
    <#local additionalClass="${blockClass}--responsive ${blockClass}--loading ${additionalClass}" />
    <#local additionalAttributes += {"data-cm-responsive-media": responsiveData} />
  </#if>
  <#if asBackground>
    <div class="${blockClass} ${blockClass}--background ${additionalClass}"
         style="background-image: url(${src});"
         <@utils.renderAttr attr=additionalAttributes ignore=["class", "style"]/>
         <@preview.metadata data=metadata />>
    </div>
  <#else>
    <img src="${src}"
         alt="${alt}"
         class="${blockClass} ${blockClass}--loading ${additionalClass}"
         <@utils.renderAttr attr=additionalAttributes ignore=["src", "alt", "class"]/>
         <@preview.metadata data=metadata />>
  </#if>
</#macro>

<#macro _renderVideoOrAudio tag
                            link
                            additionalClass=""
                            playerSettings={}
                            metadata=[]
                            preload=false
                            attr={}>
  <#-- @ftlvariable name="playerSettings" type="com.coremedia.blueprint.common.player.PlayerSettings" -->

  <#--
   Provide fallback values as long as "cm.localParameter" is used to fill the parameters of the macro.
   Can be removed as soon as this is not supported anymore.

   Important: These values need to be kept in sync with the signature of the macro
   -->
  <#local additionalClass=cm.notUndefined(additionalClass, "") />
  <#local metadata=cm.notUndefined(metadata, []) />
  <#local preload=cm.notUndefined(preload, false) />
  <#local attr=cm.notUndefined(attr, {}) />

  <#compress>
    <video src="${link}" data-cm-${tag}
           class="cm-${tag} ${additionalClass}"
      <#if !playerSettings.hideControls>controls</#if>
      <#if playerSettings.autoplay>autoplay</#if>
      <#if playerSettings.loop>loop</#if>
      <#if playerSettings.muted>muted</#if>
      <#if preload>preload="auto"</#if>
      <@utils.renderAttr attr=attr />
      <@preview.metadata data=metadata/>>
      <#-- making this explicit, concatinated resource bundle keys are not a good idea -->
      <#if tag == "video">
        ${cm.getMessage("error_video_not_available")}
      </#if>
      <#if tag == "audio">
        ${cm.getMessage("error_audio_not_available")}
      </#if>
    </video>
  </#compress>
</#macro>

<#macro renderVideo link
                    additionalClass=""
                    playerSettings={}
                    metadata=[]
                    preload=false
                    attr={}>
  <#-- @ftlvariable name="playerSettings" type="com.coremedia.blueprint.common.player.PlayerSettings" -->
  <@_renderVideoOrAudio tag="video"
                        link=link
                        additionalClass=additionalClass
                        playerSettings=playerSettings
                        metadata=metadata
                        preload=preload
                        attr=attr/>
</#macro>

<#macro renderAudio link
                    additionalClass=""
                    playerSettings={}
                    metadata=[]
                    preload=false
                    attr={}>
  <#-- @ftlvariable name="playerSettings" type="com.coremedia.blueprint.common.player.PlayerSettings" -->
  <@_renderVideoOrAudio tag="audio"
                        link=link
                        additionalClass=additionalClass
                        playerSettings=playerSettings
                        metadata=metadata
                        preload=preload
                        attr=attr/>
</#macro>

<#macro renderEmptyMedia blockClass=defaultBlockClass
                         additionalClass=""
                         metadata=[]>
  <#--
   Provide fallback values as long as "cm.localParameter" is used to fill the parameters of the macro.
   Can be removed as soon as this is not supported anymore.

   Important: These values need to be kept in sync with the signature of the macro
  -->
  <#local teaserBlockClass=cm.notUndefined(blockClass, defaultBlockClass) />
  <#local additionalClass=cm.notUndefined(additionalClass, "") />
  <#local metadata=cm.notUndefined(metadata, []) />

  <div class="${blockClass} ${blockClass}--missing ${additionalClass}"<@preview.metadata data=metadata />></div>
</#macro>
