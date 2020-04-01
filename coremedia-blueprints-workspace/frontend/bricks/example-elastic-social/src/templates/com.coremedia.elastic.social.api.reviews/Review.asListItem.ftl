<#-- @ftlvariable name="self" type="com.coremedia.elastic.social.api.reviews.Review" -->
<#-- @ftlvariable name="reviewsResult" type="com.coremedia.blueprint.elastic.social.cae.controller.ReviewsResult" -->

<li class="cm-collection__item">
<@cm.include self=self params={
"reviewingAllowed": reviewsResult.isWritingContributionsAllowed()
} />
</li>