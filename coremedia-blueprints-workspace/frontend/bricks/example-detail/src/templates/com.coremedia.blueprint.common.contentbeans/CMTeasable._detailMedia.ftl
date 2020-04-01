<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMTeasable" -->

<#import "*/node_modules/@coremedia/brick-slick-carousel/src/freemarkerLibs/slickCarousel.ftl" as slickCarousel />

<#--
  Template Description:

  Renders all assigned items in the "media" property in a slideshow.
-->

<#assign blockClass=cm.localParameters().blockClass!"cm-details" />

<@slickCarousel.render items=self.media
                       itemsView="_header"
                       itemsParams={
                         "renderTitle": false,
                         "renderText": false
                       }
                       innerArrows=true
                       additionalClass="${blockClass}__medias"
                       metadata="pictures" />
