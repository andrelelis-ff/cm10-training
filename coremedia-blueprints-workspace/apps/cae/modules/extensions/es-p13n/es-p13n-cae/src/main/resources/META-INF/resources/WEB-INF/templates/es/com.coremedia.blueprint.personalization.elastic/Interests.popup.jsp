<%@ page contentType="text/html; charset=UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="cm" uri="http://www.coremedia.com/2004/objectserver-1.0-2.0" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pbe" uri="http://www.coremedia.com/pbe" %>
<%--@elvariable id="self" type="com.coremedia.blueprint.personalization.elastic.Interests"--%>
<%--@elvariable id="cmpage" type="com.coremedia.blueprint.common.contentbeans.Page"--%>

<pbe:contextinfo var="myPbeId" resource="${self.action.content.id}" nodeAttr="id"/>
<a ${myPbeId} href="#" class="openProfile social icon-torso"><c:out value="${self.action.title}"/></a>

<div id="profile" class="profileBox" title="${self.action.title}">
  <ul>

    <c:set var="implicitSubjectTaxonomies" value="${self.implicitSubjectTaxonomies}"/>
    <c:if test="${not empty implicitSubjectTaxonomies}">
      <li class="first">
        <h4><fmt:message key="profile.subject.taxnomies"/></h4>
        <ul>
          <c:forEach items="${implicitSubjectTaxonomies}" var="item" end="10">
            <li><cm:include self="${item.key}" view="asLink"/>: <fmt:formatNumber value="${item.value*100}" pattern="0"/>%</li>
          </c:forEach>
        </ul>
      </li>
    </c:if>

    <c:set var="implicitLocationTaxonomies" value="${self.implicitLocationTaxonomies}"/>
    <c:if test="${not empty implicitLocationTaxonomies}">
      <li>
        <h4><fmt:message key="profile.location.taxnomies"/></h4>
        <ul>
          <c:forEach items="${implicitLocationTaxonomies}" var="item" end="10">
            <li><cm:include self="${item.key}" view="asLink"/>: <fmt:formatNumber value="${item.value*100}" pattern="0"/>%</li>
          </c:forEach>
        </ul>
      </li>
    </c:if>

    <c:set var="explicitUserInterests" value="${self.explicitUserInterests}"/>
    <c:if test="${not empty explicitUserInterests}">
      <li>
        <h4><fmt:message key="profile.interests"/></h4>
        <ul>
          <c:forEach items="${explicitUserInterests}" var="item">
            <li><cm:include self="${item}" view="asLink"/></li>
          </c:forEach>
        </ul>
      </li>
    </c:if>
    <%--c:set var="elasticSocialPlugin" value="${cmpage.aspectByName['elasticSocialPlugin']}"/>
    <c:set value="${elasticSocialPlugin.currentUser}" var="user"/>
    <c:if test="${not empty user}">
      <li class="last">
        <h4><fmt:message key="profile.social.activity"/></h4>
        <ul>
          <li>
            <fmt:message key="profile.amount.comments"/> ${amountOfOwnComments}
          </li>
          <li>
            <fmt:message key="profile.amount.ratings"/> ${amountOfOwnRatings}
          </li>
        </ul>
      </li>
    </c:if--%>
  </ul>
</div>


