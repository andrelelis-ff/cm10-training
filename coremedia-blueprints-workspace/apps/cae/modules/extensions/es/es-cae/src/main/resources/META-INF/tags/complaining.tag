<%@ taglib prefix="cm" uri="http://www.coremedia.com/2004/objectserver-1.0-2.0"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="social" uri="http://www.coremedia.com/elastic/social"
%>
<%@ attribute name="value" required="true" type="java.lang.Boolean" description="The complain value" %>
<%@ attribute name="id" required="true" description="The DHTML id prefix for this component" %>
<%@ attribute name="collection" type="java.lang.String" required="true" %>
<%@ attribute name="itemId" type="java.lang.String" required="true" %>
<%@ attribute name="navigationId" type="java.lang.String" required="true" %>
<%@ attribute name="customClass" type="java.lang.String" required="false" %>
<%@ attribute name="path" type="java.lang.String" required="true" %>

<span id="complainTag_${id}" class="complaint">
  <c:choose>
    <c:when test="${value}">
      <a class="enabled complaint button ${customClass}" id="complainAnchor_${id}" style="display:none"
         onclick="c_${id}.complain(complainerId, true);"><fmt:message key="comment-complaint"/></a>
      <a class="enabled uncomplaint button ${customClass}" id="uncomplainAnchor_${id}"
         onclick="c_${id}.complain(complainerId, false);"><fmt:message key="comment-uncomplaint"/></a>
    </c:when>
    <c:otherwise>
      <a class="enabled complaint button ${customClass}" id="complainAnchor_${id}"
         onclick="c_${id}.complain(complainerId, true);"><fmt:message key="comment-complaint"/></a>
      <a class="enabled uncomplaint button ${customClass}" id="uncomplainAnchor_${id}" style="display:none"
         onclick="c_${id}.complain(complainerId, false);"><fmt:message key="comment-uncomplaint"/></a>
    </c:otherwise>
  </c:choose>
</span>
<cm:link var="complainUrl" target="${path}" escape="false"/>
<script type="text/javascript">
  var c_${id} = new com.coremedia.rating.HtmlComplaintControl(
          '${complainUrl}', '${id}', '${collection}', '${itemId}', '${navigationId}',
          'complainAnchor_${id}', 'uncomplainAnchor_${id}');
  var complainerId = '${social:getCurrentGuid()}';
</script>


