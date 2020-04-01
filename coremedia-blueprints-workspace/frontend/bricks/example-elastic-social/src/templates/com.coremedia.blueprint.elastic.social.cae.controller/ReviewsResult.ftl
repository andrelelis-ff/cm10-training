<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/><#-- could be used as fragment -->
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.elastic.social.cae.controller.ReviewsResult" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />
<#import "../../freemarkerLibs/elastic-social.ftl" as elasticSocial />

<#if self.isEnabled()>
  <#assign reviewsId=bp.generateId("cm-reviews-") />
  <div class="cm-reviews" id="${reviewsId}" data-cm-refreshable-fragment='{"url": "${cm.getLink(self)}"}'>
    <h3 class="cm-reviews__headline">${cm.getMessage('reviews_title')}</h3>

    <#-- review summary -->
    <#assign numberOfOnlineReviews=self.getNumberOfOnlineReviews()!0 />
    <#if (numberOfOnlineReviews > 0)>
      <div class="cm-reviews__average-rating cm-ratings-average" itemscope="itemscope" itemtype="http://data-vocabulary.org/Review-aggregate">

        <#assign averageRating=(self.getAverageRating())!0 />

        <div class="cm-ratings-average__header">
          <div class="cm-ratings-average__rating">
            <div class="cm-ratings-average__stars--back"></div>
            <div class="cm-ratings-average__stars--front" style="width: ${(averageRating / es.getReviewMaxRating() * 100)?string("0.##")}%"></div>
          </div>

          <span class="cm-ratings-average__text" itemprop="rating" itemscope="itemscope" itemtype="http://data-vocabulary.org/Rating">
            <@cm.message key="reviews_average_symbol" /> <span itemprop="average">${averageRating?string("0.##")}</span> <@cm.message key="reviews_average_out_of" /> <span itemprop="best">${es.getReviewMaxRating()}</span>
          </span>
          <span class="cm-ratings-average__votes">
            <#assign ratingsLabel="" />
            <#if (numberOfOnlineReviews == 1)>
              <#assign ratingsLabel=cm.getMessage("reviews_average_ratings_singular") />
            <#else>
              <#assign ratingsLabel=cm.getMessage("reviews_average_ratings") />
            </#if>
            (<span itemprop="votes">${numberOfOnlineReviews}</span> ${ratingsLabel})
          </span>
        </div>

        <table class="cm-ratings-average__details cm-rating-statistics">
          <#assign totalNumberOfReviews=(self.getNumberOfOnlineReviews()!0) />
          <#list es.getReviewMaxRating()..1 as currentRating>
            <#assign currentNumber=(self.getNumberOfOnlineReviewsFor(currentRating))!0 />
            <#assign percentage=0 />
            <#if (totalNumberOfReviews > 0)>
              <#assign percentage=(currentNumber * 100 / totalNumberOfReviews) />
            </#if>
            <#assign indicatorLabel="" />
            <#if (currentRating == 1)>
              <#assign indicatorLabel=cm.getMessage("reviews_average_indicator_singular") />
            <#else>
              <#assign indicatorLabel=cm.getMessage("reviews_average_indicator") />
            </#if>
            <tr class="cm-rating-statistic">
              <td class="cm-rating-statistic__column">${currentRating} ${indicatorLabel}</td>
              <td class="cm-rating-statistic__column cm-rating-statistic__column--rating-bar">
                <div class="cm-rating-bar">
                  <div class="cm-rating-bar__filled" style="width: ${percentage}%;"></div>
                </div>
              </td>
              <td class="cm-rating-statistic__column">${currentNumber}</td>
            </tr>
          </#list>
        </table>

        <#-- hidden elements -->
        <span itemprop="count" class="cm-ratings-average__count">${numberOfOnlineReviews}</span>
        <#if self.target?has_content>
          <span itemprop="itemreviewed" class="cm-ratings-average__target">${self.target!""}</span>
        </#if>
      </div>
    <#else>
      <#if !self.isReadOnly()>
        <p class="cm-reviews__info">
          <#assign numberOfReviews=self.getNumberOfOnlineReviews()!0 />
          <#switch numberOfReviews>
            <#case 0>
              <@cm.message key="reviews_no_reviews" />
              <#break>
            <#case 1>
              <@cm.message key="reviews_headline_singular" />
              <#break>
            <#default>
              <@cm.message key="reviews_headline" args=[numberOfReviews] />
          </#switch>
        </p>
      </#if>
    </#if>

    <#-- write a review -->
    <#if self.isWritingContributionsAllowed()>
      <#-- output of dynamic, non-review specific information -->
      <@elasticSocial.notification type="inactive" text="" additionalClasses=["cm-reviews__notification"] attr={"data-cm-notification": '{"path": ""}'} />
      <#if (es.hasUserWrittenReview(self.getTarget()))!false>
        <#-- hide button instead of an diabled one -->
        <#--<div class="cm-reviews__toolbar cm-toolbar cm-toolbar--reviews">
          <@components.button text=cm.getMessage(es.messageKeys.REVIEWS_WRITE) attr={"data-cm-button--review": '{"disabled": true}', "classes": ["cm-button--disabled"], "disabled": ""} />
        </div>-->
        <@elasticSocial.notification type="info" text=cm.getMessage("reviewForm_already_reviewed") additionalClasses=["cm-reviews__notification"] attr={"data-cm-reviews-notification-type": "ALREADY_REVIEWED"} />
      <#else>
        <div class="cm-reviews__toolbar cm-toolbar cm-toolbar--reviews">
          <@components.button text=cm.getMessage("reviews_write") attr={"data-cm-button--review": ""} />
        </div>
        <@cm.include self=self view="reviewForm" />
      </#if>
    <#elseif self.isWritingContributionsEnabled() && es.isAnonymousUser()>
      <@elasticSocial.notification type="info" text=cm.getMessage("reviewForm_not_logged_in") additionalClasses=["cm-reviews__notification"] attr={"data-cm-reviews-notification-type": "LOGIN_REQUIRED"} />
      <#assign loginFlow=es.getLogin() />
      <#if loginFlow?has_content && !cm.isUndefined(loginFlow)>
        <div class="cm-reviews__toolbar cm-toolbar cm-toolbar--reviews">
          <@cm.include self=loginFlow view="asButtonGroup" />
        </div>
      </#if>
    </#if>

    <#assign reviews=self.reviews />
      <#if reviews?has_content>
        <ul class="cm-collection cm-collection--reviews cm-reviews__list">
          <#list reviews as wrapper>
            <@cm.include self=wrapper!cm.UNDEFINED view="asListItem" params={"reviewsResult": self} />
          </#list>
        </ul>
      </#if>
  </div>
</#if>
