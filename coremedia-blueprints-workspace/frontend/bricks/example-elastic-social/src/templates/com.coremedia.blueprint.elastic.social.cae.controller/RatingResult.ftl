<@cm.responseHeader name="Content-Type" value="text/html; charset=UTF-8"/><#-- could be used as fragment -->
<#-- @ftlvariable name="self" type="com.coremedia.blueprint.elastic.social.cae.controller.RatingResult" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />
<#import "../../freemarkerLibs/elastic-social.ftl" as elasticSocial />

<#if self.isEnabled()>
  <#assign ratingsId=bp.generateId("cm-ratings-") />
  <div class="cm-ratings" id="${ratingsId}" data-cm-refreshable-fragment='{"url": "${cm.getLink(self)}"}'>

    <#-- rating summary -->
    <#assign numberOfRatings=self.numberOfRatings!0 />
    <#if (numberOfRatings > 0)>
      <div class="cm-ratings__average-rating cm-ratings-average" itemscope="itemscope" itemtype="http://data-vocabulary.org/Rating-aggregate">

        <#assign averageRating=(self.getAverageRating())!0 />
        <#assign averageRatingRounded=averageRating?round />

        <div class="cm-ratings-average__header">

          <span class="cm-ratings-average__rating cm-rating">
            <#list es.getMaxRating()..1 as currentRating>
              <#assign classRatingIndicator="" />
              <#if currentRating == averageRatingRounded>
                <#assign classRatingIndicator=" cm-rating-indicator--active" />
              </#if>
                <div class="cm-rating__option cm-rating-indicator${classRatingIndicator}">${currentRating}</div>
            </#list>
          </span>
          <span class="cm-ratings-average__text" itemprop="rating" itemscope="itemscope" itemtype="http://data-vocabulary.org/Rating">
            <@cm.message key="ratings_average_symbol" /> <span itemprop="average">${averageRating?string("0.##")}</span> <@cm.message key="ratings_average_out_of" /> <span itemprop="best">${es.getMaxRating()}</span>
          </span>
          <span class="cm-ratings-average__votes">
            <#assign ratingsLabel="" />
            <#if (numberOfRatings == 1)>
              <#assign ratingsLabel=cm.getMessage("rating_number_of_ratings_singular") />
            <#else>
              <#assign ratingsLabel=cm.getMessage("rating_number_of_ratings") />
            </#if>
            (<span itemprop="votes">${numberOfRatings}</span> ${ratingsLabel})
          </span>
        </div>

    <#else>
      <#if !self.isReadOnly()>
        <h3 class="cm-ratings__title">
          <#assign numberOfRatings=self.getNumberOfRatings()!0 />
          <#switch numberOfRatings>
            <#case 0>
              <@cm.message key="ratings_no_ratings" />
              <#break>
            <#case 1>
              <@cm.message key="ratings_headline_singular" />
              <#break>
            <#default>
              <@cm.message key="ratings_headline" args=[numberOfRatings] />
          </#switch>
        </h3>
      </#if>
    </#if>

    <#-- write a rating -->
    <#if self.isWritingContributionsAllowed()>
      <#-- output of dynamic, non-rating specific information -->
      <@elasticSocial.notification type="inactive" text="" additionalClasses=["cm-ratings__notification"] attr={"data-cm-notification": '{"path": ""}'} />
      <#if (es.hasUserRated(self.getTarget()))!false>
        <div class="cm-ratings__toolbar cm-toolbar cm-toolbar--ratings">
          <@components.button text=cm.getMessage("ratings_write") attr={"class": "cm-button cm-button--small cm-button--rating cm-button--disabled"} />
        </div>
        <@elasticSocial.notification type="info" text=cm.getMessage("reviewForm_already_rated") additionalClasses=["cm-ratings__notification"] attr={"data-cm-ratings-notification-type": "ALREADY_RATED"} />
      <#else>
        <div class="cm-ratings__toolbar cm-toolbar cm-toolbar--ratings">
          <@components.button text=cm.getMessage("ratings_write") attr={"class": "cm-button cm-button--small cm-button--rating", "data-cm-button--rating": ''} />
        </div>
        <@cm.include self=self view="ratingForm" />
      </#if>
    <#elseif self.isWritingContributionsEnabled() && es.isAnonymousUser()>
      <@elasticSocial.notification type="info" text=cm.getMessage("ratingForm_not_logged_in") additionalClasses=["cm-ratings__notification"] attr={"data-cm-ratings-notification-type": "LOGIN_REQUIRED"} />
      <div class="cm-ratings__toolbar cm-toolbar cm-toolbar--ratings">
        <@cm.include self=es.getLogin()!cm.UNDEFINED view="asButton" />
      </div>
    </#if>

  </div>
</#if>
