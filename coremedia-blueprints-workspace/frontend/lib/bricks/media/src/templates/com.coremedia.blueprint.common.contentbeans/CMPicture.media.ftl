<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMPicture" -->
<#-- @ftlvariable name="classBox" type="java.lang.String" -->
<#-- @ftlvariable name="classMedia" type="java.lang.String" -->
<#-- @ftlvariable name="disableCropping" type="java.lang.Boolean" -->
<#-- @ftlvariable name="background" type="java.lang.Boolean" -->
<#-- @ftlvariable name="metadata" type="java.util.List" -->
<#-- @ftlvariable name="metadataMedia" type="java.util.List" -->
<#-- @ftlvariable name="additionalAttr" type="java.util.Map" -->

<#import "../../freemarkerLibs/media.ftl" as media />

<#assign classBox=cm.localParameters().classBox!""/>
<#assign classMedia=cm.localParameters().classMedia!""/>
<#assign disableCropping=cm.localParameters().disableCropping!false/>
<#assign background=cm.localParameters().background!false/>
<#assign metadata=cm.localParameters().metadata![]/>
<#assign metadataMedia=cm.localParameters().metadataMedia![]/>
<#assign additionalAttr=cm.localParameters().additionalAttr!{}/>

<div class="${classBox}"<@preview.metadata data=metadata + [self.content]/>>
  <#if self.data?has_content>
    <#-- adding an 1x1px transparent png as default to avoid a broken image icon in chrome -->
    <#assign imageLink="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="/>
    <#assign responsiveData=""/>

    <#-- decide if responsiveImage functionality is to be used or uncropped image will be shown -->
    <#if self.disableCropping || disableCropping>
      <#-- A) Cropping disabled, display original image in full size -->
      <#assign imageLink=cm.getLink(self.data)/>
    <#else>
      <#-- B) display responsive image -->
      <#assign responsiveData=bp.responsiveImageLinksData(self)!""/>
    </#if>

    <#-- use high resolution images -->
    <#assign retinaEnabled=bp.setting(cmpage, "enableRetinaImages", false)/>

    <#-- alt is the content name by default -->
    <#assign alt=(self.content.name)!""/>
    <#-- if alt property is set, use it as alt -->
    <#if self.alt?has_content>
      <#assign alt=self.alt />
    </#if>

    <@media.renderPicture asBackground=background
                          additionalClass=classMedia
                          src=imageLink
                          alt=alt
                          enableRetina=retinaEnabled
                          responsiveData=responsiveData
                          metadata=metadataMedia + ["properties.data"]
                          additionalAttributes=additionalAttr />
  </#if>
</div>
