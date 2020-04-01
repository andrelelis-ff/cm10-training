<#-- @ftlvariable name="self" type="com.coremedia.elastic.social.api.reviews.Review" -->
<#-- @ftlvariable name="reviewView" type="java.lang.String" -->
<#-- @ftlvariable name="reviewingAllowed" type="java.lang.Boolean" -->

<#import "*/node_modules/@coremedia/brick-utils/src/freemarkerLibs/components.ftl" as components />
<#import "../../freemarkerLibs/elastic-social.ftl" as elasticSocial />

<#assign reviewView=es.getReviewView(self) />
<#assign title=self.title!"" />
<#assign rating=self.rating!"" />
<#assign strDate=self.creationDate?date?string.long />
<#assign strDateTechnical=self.creationDate?string("yyyy-MM-dd") />
<#assign isUserAnonymous=es.isAnonymous(self.author) />

<#assign strAuthorName=self.authorName!"" />
<#if !strAuthorName?has_content>
  <#if !self.author?has_content || isUserAnonymous>
    <#assign strAuthorName=cm.getMessage("review_author_anonymous") />
  <#else>
    <#assign strAuthorName=(self.author.name)!"" />
  </#if>
</#if>

<div class="cm-review" data-cm-review-id="${self.id}" itemscope="itemscope" itemtype="http://data-vocabulary.org/Review" <@preview.metadata self/>>
  <#if (self.target)?has_content>
    <span itemprop="itemreviewed" class="cm-review__target">${self.target}</span>
  </#if>

  <#if ["default", "undecided", "rejected"]?seq_contains(reviewView)>
    <div class="cm-review__header">
      <#if !isUserAnonymous && self.author.image?has_content>
        <#assign elasticSocialSettings=bp.setting(cmpage!cm.UNDEFINED, "elasticSocial")/>
        <#assign link=cm.getLink(self.author.image {"transform":true, "width":elasticSocialSettings.userImageThumbnailWidth!40?int, "height": elasticSocialSettings.userImageCommentThumbnailHeight!40?int})/>
        <img src="${link}" class="cm-review__user-image">
      <#else>
        <div class="cm-review__user-image cm-review__user-image--default"></div>
      </#if>
      <div class="cm-review__rating-title">
        <span class="cm-review__rating cm-rating" itemprop="rating">
          <#list es.getReviewMaxRating()..1 as currentRating>
            <#assign classRatingIndicator="" />
            <#if currentRating == rating>
              <#assign classRatingIndicator=" cm-rating-indicator--active" />
            </#if>
            <div class="cm-rating__option cm-rating-indicator${classRatingIndicator}">${currentRating}</div>
          </#list>
          <meta itemprop="value" content="${rating}" property="">
          <meta itemprop="best" content="${es.getReviewMaxRating()}" property="">
        </span>
        <span class="cm-review__title" itemprop="summary">${title!""}</span>
      </div>
      <div class="cm-review__author-date">
        ${cm.getMessage('review_author_by')}
        <span class="cm-review__author" itemprop="reviewer">${strAuthorName!""}</span>
        ${cm.getMessage('review_author_on')}
        <time class="cm-review__date" itemprop="dtreviewed" datetime="${strDateTechnical}">${strDate}</time>
      </div>
    </div>
    <div class="cm-review__text cm-readmore" data-cm-readmore='{"lines": 5, "text": "${cm.getMessage("review_more")}"}'>
      <div class="cm-readmore__wrapper" itemprop="description">
        ${self.textAsHtml?no_esc}
      </div>
      <div class="cm-readmore__buttonbar">
        <@components.button baseClass="" text=cm.getMessage("review_more") attr={"class": "cm-readmore__button-more"} />
        <@components.button baseClass="" text=cm.getMessage("review_less") attr={"class": "cm-readmore__button-less"} />
      </div>
    </div>
  </#if>

  <#-- At least one dynamic notification is rendered -->
  <#if ["undecided"]?seq_contains(reviewView)>
    <@elasticSocial.notification type="info" text=cm.getMessage("review_approval_undecided") additionalClasses=["cm-review__notification"] attr={"data-cm-notification": '{"path": ""}', "data-cm-contribution-notification-type": "UNDECIDED"} />
  <#elseif ["rejected"]?seq_contains(reviewView)>
    <@elasticSocial.notification type="warning" text=cm.getMessage("review_approval_rejected") additionalClasses=["cm-review__notification"] attr={"data-cm-notification": '{"path": ""}', "data-cm-contribution-notification-type": "REJECTED"} />
  <#elseif ["deleted"]?seq_contains(reviewView)>
  <#-- not in use -->
    <@elasticSocial.notification type="info" text=cm.getMessage("review_deleted") additionalClasses=["cm-review__notification"] attr={"data-cm-notification": '{"path": ""}'} />
  <#else>
    <@elasticSocial.notification type="inactive" additionalClasses=["cm-review__notification"] attr={"data-cm-notification": '{"path": ""}'} />
  </#if>
</div>
