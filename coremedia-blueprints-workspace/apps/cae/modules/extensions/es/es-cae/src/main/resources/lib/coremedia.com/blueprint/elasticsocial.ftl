<#ftl strip_whitespace=true>
<#-- @ftlvariable name="elasticSocialFreemarkerFacade" type="com.coremedia.blueprint.elastic.social.cae.tags.ElasticSocialFreemarkerFacade" -->

<#-- -------------------------------------------------------------------------------------------------------------------
 *
 * Please check the section "CoreMedia FreeMarker API" in chapter "Reference" in the "Frontend Developer Guide" for
 * details and examples for the following directives.
 * Any changes, additions or removals need to be documented in the manual.
 *
 ------------------------------------------------------------------------------------------------------------------- -->

<#-- GET PAGE CONFIGURATION -->
<#function getElasticSocialConfiguration page>
  <#return elasticSocialFreemarkerFacade.getElasticSocialConfiguration(page)>
</#function>

<#-- CURRENT USER CHECK -->
<#function isAnonymousUser>
  <#return elasticSocialFreemarkerFacade.isAnonymousUser()>
</#function>

<#-- COMMUNITY USER CHECK -->
<#function isAnonymous communityUser>
  <#return elasticSocialFreemarkerFacade.isAnonymous(communityUser)>
</#function>

<#-- GET TENANT -->
<#function getCurrentTenant>
  <#return elasticSocialFreemarkerFacade.getCurrentTenant()>
</#function>

<#-- GET WRITTEN REVIEW -->
<#function hasUserWrittenReview target>
  <#return elasticSocialFreemarkerFacade.hasUserWrittenReview(target)>
</#function>

<#-- GET RATING SCORE -->
<#function hasUserRated target>
  <#return elasticSocialFreemarkerFacade.hasUserRated(target)>
</#function>

<#-- GET COMMENT -->
<#function getCommentView comment>
  <#local isPreview=preview.isPreviewCae() />
  <#local currentUser=getCurrentUser() />

  <#local view="default" />
  <#switch comment.state>
    <#case "NEW">
      <#local view="undecided" />
      <#if (!isPreview) && currentUser?has_content && comment.author?has_content && (currentUser.id != comment.author.id)>
        <#local view="deleted" />
      </#if>
      <#break>
    <#case "NEW_ONLINE">
    <#case "APPROVED">
      <#-- nothing to do -->
      <#break>
    <#case "REJECTED">
      <#local view="rejected" />
      <#-- only applied for live if user != author, which would already be filtered if no subcomments exist -->
      <#if (!isPreview) && currentUser?has_content && comment.author?has_content && (currentUser.id != comment.author.id)>
        <#local view="deleted" />
      </#if>
      <#break>
    <#case "IGNORED">
      <#-- only applied if in preview or if user != author, which would already be filtered of no subcomments exist -->
      <#if isPreview || (currentUser?has_content && comment.author?has_content && (currentUser.id != comment.author.id))>
        <#local view="deleted" />
      </#if>
      <#break>
  </#switch>

  <#return view />
</#function>

<#-- GET COMMENT REVIEW -->
<#function getReviewView review>
  <#return getCommentView(review) />
</#function>

<#-- MAX RATING SCORE -->
<#function getMaxRating>
  <#return 5 />
</#function>

<#-- MAX REVIEW RATING SCORE -->
<#function getReviewMaxRating>
  <#return 5 />
</#function>

<#-- LOGIN CHECK -->
<#function getLogin>
  <#return bp.setting(cmpage!cm.UNDEFINED, "flowLogin", cm.UNDEFINED) />
</#function>

<#-- USER SPECIFIC DATA -->
<#macro complaining value id collection itemId navigationId customClass="">
<span id="complainTag_${id}" class="complaint">
  <#if value?has_content>
      <a class="enabled complaint button ${customClass}" id="complainAnchor_${id}" style="display:none"
         onclick="c_${id}.complain(complainerId, true);"><@cm.message "comment-complaint"/></a>
      <a class="enabled uncomplaint button ${customClass}" id="uncomplainAnchor_${id}"
         onclick="c_${id}.complain(complainerId, false);"><@cm.message "comment-uncomplaint"/></a>
  <#else>
      <a class="enabled complaint button ${customClass}" id="complainAnchor_${id}"
         onclick="c_${id}.complain(complainerId, true);"><@cm.message "comment-complaint"/></a>
      <a class="enabled uncomplaint button ${customClass}" id="uncomplainAnchor_${id}" style="display:none"
         onclick="c_${id}.complain(complainerId, false);"><@cm.message "comment-uncomplaint"/></a>
  </#if>
</span>
<#local complainUrl=cm.getLink("/elastic/social/complaint")/>
<#outputformat "JavaScript">
<script type="text/javascript">
  var c_${id} = new com.coremedia.rating.HtmlComplaintControl(
          '${complainUrl}', '${id}', '${collection}', '${itemId}', '${navigationId}',
          'complainAnchor_${id}', 'uncomplainAnchor_${id}');
  var complainerId = '${elasticSocialFreemarkerFacade.getCurrentGuid()}';
</script>
</#outputformat>
</#macro>


<#-- --- INTERNAL/PRIVATE ------------------------------------------------------------------------------------------ -->


<#-- PRIVATE -->
<#function getCurrentUser>
  <#return elasticSocialFreemarkerFacade.getCurrentUser()>
</#function>


<#-- ---  UNUSED --------------------------------------------------------------------------------------------------- -->


<#-- UNUSED -->
<#function hasComplaintForCurrentUser id collection>
  <#return elasticSocialFreemarkerFacade.hasComplaintForCurrentUser(id, collection)>
</#function>

<#-- UNUSED -->
<#function getCommentsResult target>
  <#return elasticSocialFreemarkerFacade.getCommentsResult(target)>
</#function>

<#-- UNUSED -->
<#function getReviewsResult target>
  <#return elasticSocialFreemarkerFacade.getReviewsResult(target)>
</#function>

<#-- UNUSED -->
<#function getNumberOfComments target>
  <#return elasticSocialFreemarkerFacade.getNumberOfComments(target)>
</#function>

<#-- UNUSED -->
<#function isLoginAction bean>
  <#return elasticSocialFreemarkerFacade.isLoginAction(bean)>
</#function>
