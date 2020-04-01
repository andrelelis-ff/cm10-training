<%@ tag body-content="scriptless" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="type" required="true" description="Type of notification" %>
<%@ attribute name="text" required="false" description="Optional plain text to show (html will be escaped), content provided via body will not be escaped" %>
<%@ attribute name="dismissable" required="true" description="Show dismiss-button or not?" %>
<%@ attribute name="title" required="false" description="Optional title to display" %>

<div class='notification <c:out value="${type}" />'>
  <c:if test="${!empty title}">
    <h3 class="notification-headline"><c:out value="${title}" /></h3>
  </c:if>
  <c:if test="${!empty text}">
    <c:out value="${text}" escapeXml="false" />
  </c:if>
  <jsp:doBody/>
  <c:if test="${dismissable}">
    <div class='button-dismiss'></div>
  </c:if>
</div>
