<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMPicture" -->

<#--
    Template Description:

    Displays a picture in original size. Please check the brick "responsive-images" for a more detailed version.
-->

<#if self.data?has_content>
  <img class="cm-picture" src="${cm.getLink(self.data)}" width="100%" alt="${self.title!""}">
</#if>


