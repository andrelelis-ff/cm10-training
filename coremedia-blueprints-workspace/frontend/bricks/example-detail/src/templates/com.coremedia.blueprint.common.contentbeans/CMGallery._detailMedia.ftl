<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.contentbeans.CMGallery" -->

<#import "*/node_modules/@coremedia/brick-slick-carousel/src/freemarkerLibs/slickCarousel.ftl" as slickCarousel />

<#--
  Template Description:

  Same as CMTeasable._detailMedia.ftl but instead of "media" the "items" property is used.
-->

<#assign blockClass=cm.localParameters().blockClass!"cm-details" />

<@slickCarousel.render items=self.items
                       itemsView="_header"
                       itemsParams={
                         "renderTitle": false,
                         "renderText": false
                       }
                       innerArrows=true
                       additionalClass="${blockClass}__medias"
                       metadata="pictures" />
