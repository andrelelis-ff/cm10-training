<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/><#-- could be used as fragment -->
<#-- @ftlvariable name="self" type="com.coremedia.livecontext.ecommerce.catalog.Product" -->
<#-- @ftlvariable name="orientation" type="java.lang.String" -->
<#-- @ftlvariable name="types" type="java.lang.String" -->

<#import "*/node_modules/@coremedia/brick-slick-carousel/src/freemarkerLibs/slickCarousel.ftl" as slickCarousel />

<div class="cm-product-assets" <@cm.dataAttribute name="data-cm-refreshable-fragment" data={"url": cm.getLink(self, 'asAssets')} />
                               <@cm.dataAttribute name="data-cm-product-assets" data=bp.setting(self, "productAssets", {}) />>
  <#if (types == 'all' || types == 'visuals') && orientation?has_content>
    <#assign visuals=bp.createBeansFor(self.visuals)![] />
    <#if !visuals?has_content>
      <@cm.include self=self.catalogPicture view="asProductAsset" params={
        "orientation": orientation
      } />
    <#else>
      <#-- slideshow with large images -->
      <@slickCarousel.render additionalClass="cm-product-assets__slideshow"
                             slickConfig={
                               "arrows": false,
                               "swipe": false,
                               "draggable": false,
                               "touchMove": false,
                               "infinite": false
                             }
                             items=visuals
                             itemsView="asProductAsset"
                             itemsParams={
                               "modifier": orientation
                             } />
      <#-- this is the thumbnail slideshow -->
      <#if (visuals?size > 1)>
        <@slickCarousel.render additionalClass="cm-product-assets__carousel"
                               slickConfig={
                                 "slidesToShow": 4,
                                 "slidesToScroll": 1,
                                 "focusOnSelect": false,
                                 "infinite": false
                               }
                               innerArrows=true
                               items=visuals
                               itemsView="asProductAsset"
                               itemsParams={
                                 "renderAsThumbnail": true
                               } />
      </#if>
    </#if>
  </#if>

  <#-- render download list -->
  <#if (types == 'all' || types == 'downloads') >
    <#assign downloads=bp.createBeansFor(self.downloads) />
    <#if (downloads?size > 0)>
      <div class="cm-product-assets__downloads cm-product-assets-downloads">
        <h3 class="cm-product-assets-downloads__title">${cm.getMessage("product_assets_downloads")}</h3>
        <ul class="cm-product-assets-downloads__list">
          <#list downloads![] as download>
            <#if download.data?has_content>
              <li class="cm-product-assets-downloads__item">
                <a href="${cm.getLink(download.target!cm.UNDEFINED)}"<@preview.metadata data=[download.content, "properties.teaserTitle"] />>${download.teaserTitle!""}</a>
              </li>
            </#if>
          </#list>
        </ul>
      </div>
    </#if>
  </#if>

</div>
