<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.cae.PaginatedAssets" -->

<#if self.assets?has_content || self.notification?has_content>
  <div class="am-paginated-assets">
    <h2 class="am-heading-2">${cm.getMessage("am_assets")}</h2>

    <@cm.include self=self.notification!cm.UNDEFINED params={"classBox": "am-paginated-assets__notification"} />

    <div class="am-paginated-assets__assets">
      <#list self.assets as asset>
        <div class="am-paginated-assets__asset">
          <@cm.include self=asset view="asTeaser" params={"baseRequestParams": self.baseRequestParams} />
        </div>
      </#list>
    </div>

    <#if (self.pageCount > 1)>
      <ul class="am-paginated-assets__pages am-pagination">
        <li class="am-pagination__page">
          <#if (self.currentPage > 1)>
            <#assign prevPage=self.currentPage - 1 />
            <#assign linkData={
              "requestParams": self.baseRequestParams + {
                "page": prevPage
              }
            } />
            <a class="am-page-number am-page-number--prev am-page-number--link" <@cm.dataAttribute name="data-hash-based-fragment-link" data=linkData />>&lt;</a>
          <#else>
            <span class="am-page-number am-page-number--prev">&lt;</span>
          </#if>
        </li>
        <#list 1..self.pageCount as page>
          <#assign linkData={
            "requestParams": self.baseRequestParams + {
              "page": page
            }
          } />
          <li class="am-pagination__page">
            <#if self.currentPage != page>
              <a class="am-page-number am-page-number--link" <@cm.dataAttribute name="data-hash-based-fragment-link" data=linkData />>${page}</a>
            <#else>
              <span class="am-page-number am-page-number--active">${page}</span>
            </#if>
          </li>
        </#list>
        <li class="am-pagination__page">
          <#if (self.currentPage < self.pageCount)>
            <#assign nextPage=self.currentPage + 1 />
            <#assign linkData={
              "requestParams": self.baseRequestParams + {
                "page": nextPage
              }
            } />
            <a class="am-page-number am-page-number--next am-page-number--link" <@cm.dataAttribute name="data-hash-based-fragment-link" data=linkData />>&gt;</a>
          <#else>
            <span class="am-page-number am-page-number--next">&gt;</span>
          </#if>
        </li>
      </ul>
    </#if>
  </div>
</#if>
