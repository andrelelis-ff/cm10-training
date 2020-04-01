<%@ page contentType="text/html; charset=UTF-8" session="false"
%><%@ include file="/WEB-INF/includes/taglibs.jinc"
%><%--@elvariable id="self" type="com.coremedia.blueprint.analytics.webtrends.WebtrendsPageAspect"--%>
<c:if test="${self.enabled}">
  <%-- Tag help summary: http://webtrends.dbt.co.uk/wrc/help/webhelp/hlp_customizing_tag.htm --%>
  <META name="WT.oss" content="${searchAlxProperties.query}"/> <%-- Internal Search--%>
  <META name="WT.cg_n" content="${self.navigationPathSegments[1]}"/> <%-- take 2nd navigation path arc as content category --%>
  <%-- take remaining navigation path arcs as subcategory--%>
  <c:forEach items="${self.navigationPathSegments}"
             begin="2" end="${fn:length(self.navigationPathSegments) - 1}" var="cat" varStatus="stats">
    <c:if test="${!stats.first}"><c:set var="subCategories" value="${subCategories};"/></c:if>
    <c:set var="subCategories" value="${subCategories}${cat}"/>
  </c:forEach>
  <META name="WT.cg_s" content="${subCategories}"/>

  <META name="segments" content="${fn:join(p13nAlxProperties.segmentNames, ';')}"/><%-- P13N segment names --%>
  <META name="segmentIds" content="${fn:join(p13nAlxProperties.segmentIds, ';')}"/><%-- P13N segment ids --%>

  <META name="contentId" content="${self.contentId}"/>
</c:if>