<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/><#-- could be used as fragment -->
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.elastic.social.cae.controller.ReviewsResult" -->

<#if self.isEnabled()>
  <#assign numberOfReviews=self.getNumberOfOnlineReviews()!0 />
  <#if (numberOfReviews > 0)>
    <#assign averageRating=(self.getAverageRating())!0 />
    <div class="cm-rating cm-rating--average">
      <div class="cm-ratings-average__rating">
        <div class="cm-ratings-average__stars--back"></div>
        <div class="cm-ratings-average__stars--front" style="width: ${(averageRating / es.getReviewMaxRating() * 100)?string("0.##")}%"></div>
      </div>
      <span class="cm-rating__votes">${numberOfReviews}</span>
    </div>
  </#if>
</#if>
