<#-- @ftlvariable name="self" type="com.coremedia.blueprint.common.layout.Container" -->

<#--
  Template Description:

  This template renders all items as flattened list with the view "asHeroBanner" inside a slick carousel.

  @since 1907
-->

<#import "*/node_modules/@coremedia/brick-slick-carousel/src/freemarkerLibs/slickCarousel.ftl" as slickCarousel />

<#assign items=self.flattenedItems![] />

<#if (items?size > 0)>
  <@slickCarousel.render
    items=items
    itemsView="asHeroBanner"
    innerArrows=true
    slickConfig={"fade": true}
    additionalClass="cm-hero-banner-container"
  />
</#if>
