<%@ tag body-content="scriptless" %>
<%@ attribute name="path" required="true" description="The path" %>
<%@ attribute name="field" required="false" description="Optional the field (empty: global error, form: special case
for non input field, other: field error)" %>
<%@ attribute name="dismissable" required="true" description="Show dismiss-button or not?" %>
<%@ attribute name="type" required="false" description="Optional type of notification (default: 'error')" %>
<%@ attribute name="text" required="false" description="Optional text to show (default: spring error-text)" %>
<%@ attribute name="title" required="false" description="Optional title to display (default: empty)" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="bp" uri="http://www.coremedia.com/2012/blueprint" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:if test="${empty type}">
  <c:set var="type" value="error" />
</c:if>
<c:if test="${empty text}">
  <spring:bind path="${path}">
    <%-- global error --%>
    <c:if test="${empty field}">
      <c:if test="${status.errors.hasGlobalErrors()}">
        <fmt:message key="${path}.form.error" var="text"/>
      </c:if>
    </c:if>
    <%-- special case for field="form" which is supposed to create some kind of global error --%>
    <c:if test="${!empty field && field == 'form'}">
      <c:if test="${status.errors.hasErrors(field)}">
        <fmt:message key="${path}.${field}.error" var="text"/>
      </c:if>
    </c:if>
    <%-- field error --%>
    <c:if test="${!empty field && field != 'form'}">
      <c:if test="${status.errors.hasFieldErrors(field)}">
        <fmt:message key="${path}.${field}.error" var="text"/>
      </c:if>
    </c:if>
  </spring:bind>
</c:if>

<c:if test="${!empty text}">
  <bp:notification type="${type}" text="${text}" dismissable="${dismissable}" title="${title}" />
</c:if>