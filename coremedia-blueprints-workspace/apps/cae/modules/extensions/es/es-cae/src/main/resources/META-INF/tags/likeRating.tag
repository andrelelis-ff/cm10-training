<%@ taglib prefix="cm" uri="http://www.coremedia.com/2004/objectserver-1.0-2.0"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="social" uri="http://www.coremedia.com/elastic/social"
%>
<%@ attribute name="value" required="true" type="java.lang.Boolean" description="The like value" %>
<%@ attribute name="totalLikes" type="java.lang.Long" required="true" %>
<%@ attribute name="enabled" type="java.lang.Boolean" required="true" %>
<%@ attribute name="socialTrackingEnabled" type="java.lang.Boolean" required="false" %>
<%@ attribute name="itemId" type="java.lang.String" required="false" %>
<%@ attribute name="title" type="java.lang.String" required="false" %>
<%@ attribute name="navigationId" type="java.lang.String" required="false" %>
<%@ attribute name="id" required="true" description="The DHTML id prefix for this component" %>
<%@ attribute name="path" type="java.lang.String" required="true" %>

<c:set var="likeLabelId" value=""/>
<cm:link var="likeUrl" target="${path}" escape="false"/>
<li class="like">
  <c:choose>
    <c:when test="${enabled}">
      <c:choose>
        <c:when test="${value}">
          <a class="enabled" id="likeAnchor_${id}" style="display:none" onclick="l_${id}.like(likerId, true);">
            <fmt:message key="rating-like"/>
          </a>
          <a class="enabled" id="unlikeAnchor_${id}" onclick="l_${id}.like(likerId, false);">
            <fmt:message key="rating-unlike"/>
          </a>
        </c:when>
        <c:otherwise>
          <a class="enabled" id="likeAnchor_${id}" onclick="l_${id}.like(likerId, true);">
            <fmt:message key="rating-like"/>
          </a>
          <a class="enabled" id="unlikeAnchor_${id}" style="display:none" onclick="l_${id}.like(likerId, false);">
            <fmt:message key="rating-unlike"/>
          </a>
        </c:otherwise>
      </c:choose>
      <c:out value=" - "/>
      <c:set var="likeLabelId" value="likeLabel_${id}"/>
    </c:when>
  </c:choose>
  <c:if test="${not empty likeLabelId}">
    <c:set var="likeLabelIdAttr" value=" id=\"${likeLabelId}\""/>
  </c:if>
<span${likeLabelIdAttr}>
  <c:out value="${totalLikes} "/>
  <c:choose>
    <c:when test="${totalLikes eq 1}">
      <fmt:message key="rating-number-of-likes-singular"/>
    </c:when>
    <c:otherwise>
      <fmt:message key="rating-number-of-likes"/>
    </c:otherwise>
  </c:choose>
</span>
</li>
<c:if test="${enabled}">
  <script type="text/javascript">
    var l_${id} = new com.coremedia.rating.HtmlLikeControl(document.getElementById('liking_${id}'),
            '<c:out value="${likeUrl}"/>', '<c:out value="${itemId}"/>', '<c:out value="${navigationId}"/>',
            'likeAnchor_${id}', 'unlikeAnchor_${id}', 'likeLabel_${id}',
            '<fmt:message key="rating-number-of-likes-singular"/>', '<fmt:message key="rating-number-of-likes"/>', '${title}', ${socialTrackingEnabled});
    var likerId = '<c:out value="${social:getCurrentGuid()}"/>';
  </script>
</c:if>
