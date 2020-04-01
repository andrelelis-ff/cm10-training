<#-- @ftlvariable name="self" type="com.coremedia.blueprint.assets.cae.AssetDetails" -->

<div class="am-asset-details"<@preview.metadata data=self.asset.content />>

  <div class="am-asset-details__left">
    <@cm.include self=self.asset view="asPictureBox" params={"classBox": "am-asset-details__picture"}  />

    <#if self.asset.allSubjects?has_content>
      <ul class="am-asset-details__subject-tags">
        <#list self.asset.allSubjects as subjectTag>
          <li class="am-asset-details__subject-tag">
            <@cm.include self=subjectTag view="asSubjectTag" />
          </li>
        </#list>
      </ul>
    </#if>
  </div>

  <div class="am-asset-details__right">
    <div class="am-asset-details__info"<@preview.metadata data="properties.metadata" />>
      <#if self.metadataProperties?has_content>
        <table class="am-asset-info">
          <#list self.metadataProperties?keys as key>
            <tr class="am-asset-info__property">
              <td class="am-asset-info__property-name">${cm.getMessage("am_asset_metadata_${key}")}</td>
              <td class="am-asset-info__property-value">${self.metadataProperties[key]!""}</td>
            </tr>
          </#list>
        </table>
      </#if>
    </div>
  </div>

  <div class="am-asset-details__renditions">
    <#if self.asset.publishedRenditions?has_content>
      <h3 class="am-asset-details__renditions-title am-heading-3">${cm.getMessage("am_renditions")}</h3>
      <table class="am-asset-details__renditions-list am-renditions">
        <#list self.asset.publishedRenditions as rendition>
          <@cm.include self=rendition view="_asset-details" />
        </#list>
      </table>
    </#if>
  </div>

</div>

