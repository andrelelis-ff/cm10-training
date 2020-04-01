<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.Container" -->

<#import "*/node_modules/@coremedia/brick-slick-carousel/src/freemarkerLibs/slickCarousel.ftl" as slickCarousel />

<#--
  Template Description:

  This template renders all items as flattened list with the view "asCarouselBanner" inside a slick carousel.

  @since 1907
-->

<#assign slickConfig={

  "mobileFirst": true,

  "responsive": [
    {
      "breakpoint": 0,
      "settings": {
        "arrows": false,
        "centerMode": true,
        "centerPadding": "30px",
        "slidesToScroll": 1,
        "slidesToShow": 2
      }
    },
    {
      "breakpoint": 543,
      "settings": {
        "arrows": false,
        "centerMode": true,
        "centerPadding": "30px",
        "slidesToScroll": 1,
        "slidesToShow": 3
      }
    },
    {
      "breakpoint": 767,
      "settings": {
        "slidesToScroll": 3,
        "slidesToShow": 3
      }
    },
    {
      "breakpoint": 991,
      "settings": {
      "slidesToScroll": 4,
        "slidesToShow": 4
      }
    },
    {
      "breakpoint": 1199,
      "settings": {
        "slidesToScroll": 5,
        "slidesToShow": 5
      }
    }
  ]
}/>

<div class="cm-carousel-banner-container" <@preview.metadata data=[bp.getContainerMetadata(self),bp.getPlacementHighlightingMetaData(self)!""] />>

  <#if self.teaserTitle?has_content>
    <h2 class="cm-carousel-banner-container__headline" <@preview.metadata "properties.teaserTitle"/>>${self.teaserTitle}</h2>
  </#if>

  <@slickCarousel.render items=self.flattenedItems
                         itemsView="asCarouselBanner"
                         slickConfig=slickConfig
                         additionalClass="cm-carousel-banner-container__items cm-slick-carousel--multiple"/>
</div>
