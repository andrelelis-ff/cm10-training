<#--
  Renders the given items inside a slick carousel.

  @param items the items to render inside the carousel
  @param itemsView every item will be rendered with a cm.include call, this parameter sets the view to be used
  @param itemsParams every item will be rendered with a cm.include call, this parameter sets the params to be used
  @param slickConfig the configuration of the slick carousel (see http://kenwheeler.github.io/slick/)
  @param additionalClass one or multiple css classes to add to the outer most element
  @param metadata preview metadata to add to the outer most element

  Example:
  <@render items=self.items
           itemsView="asTeaser"
           itemsParams={ "renderCaption": false }
           slickConfig={ "slidesToShow": 3 }
           innerArrows=false
           blockClass="my-slick-carousel"
           additionalClass="multi-carousel"
           metadata=self.content />
-->
<#macro render items
               itemsView=cm.UNDEFINED
               itemsParams={}
               slickConfig={}
               innerArrows=false
               blockClass="cm-slick-carousel"
               additionalClass=""
               metadata=[]>
  <#local innerArrowsCls=innerArrows?then("${blockClass}--inner-arrows", "") />
  <div class="${blockClass} ${innerArrowsCls} ${additionalClass}" <@cm.dataAttribute name="data-cm-slick-carousel" data=slickConfig /><@preview.metadata data=metadata />>
    <#list items as item>
      <#-- this div is used by slick for the slides -->
      <div class="${blockClass}__item">
        <@cm.include self=item view=itemsView params=itemsParams + {"item": {"index": item_index, "first": item?is_first, "last": item?is_last, "length": items?size}} />
      </div>
    </#list>
  </div>
</#macro>
