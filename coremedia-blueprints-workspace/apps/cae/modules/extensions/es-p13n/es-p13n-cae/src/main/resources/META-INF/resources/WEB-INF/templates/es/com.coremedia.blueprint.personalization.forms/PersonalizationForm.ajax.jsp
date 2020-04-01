<%@ taglib prefix="cm" uri="http://www.coremedia.com/2004/objectserver-1.0-2.0" %>
<%@ page contentType="text/html; charset=UTF-8" session="false" %>
<%@ include file="/WEB-INF/includes/taglibs.jinc" %>
<%--@elvariable id="self" type="com.coremedia.blueprint.personalization.forms.PersonalizationForm"--%>
<%--@elvariable id="cmpage" type="com.coremedia.blueprint.common.contentbeans.Page"--%>
<%--@elvariable id="pReadOnly" type="java.lang.Boolean"--%>

<c:if test="${not empty self.entries}">
  <c:set var="elasticSocialPlugin" value="${cmpage.aspectByName['elasticSocialPlugin']}"/>
  <c:set value="${elasticSocialPlugin.currentUser}" var="user"/>

  <cm:link var="updateExplicitInterestsLink" target="${self}" view="ajax">
    <cm:param name="page" value="${cmpage}"/>
  </cm:link>
  <script type="text/javascript">
    $(document).ready(function () {
      bpRegisterForm('#explicitInterestsForm', '${updateExplicitInterestsLink}', '#explicitInterests');
      return false;
    });
  </script>
  <div id="explicitInterests">
    <h3><fmt:message key="userDetails.interests"/></h3>
    <form:form id="explicitInterestsForm" modelAttribute="self"
               action="${updateExplicitInterestsLink}" method="post">

      <div class="content">
        <fieldset class="onecolumn">
          <c:if test="${!pReadOnly}">
            <div class="fieldwrapper"><fmt:message key="userDetails.interests.description"/></div>
          </c:if>
          <c:if test="${self.actionSuccess}">
            <div class="notification success">
              <fmt:message key="userDetails.interests.change.success"/>
            </div>
          </c:if>

          <div class="fieldwrapper optionlist">
            <ul>
              <c:forEach items="${self.entries}" var="entry" varStatus="gridRow">
                <li>
                  <form:checkbox path="entries[${gridRow.index}].value" disabled="${pReadOnly}"/>
                  <spring:bind path="entries[${gridRow.index}].bean">
                    <label for="entries[${gridRow.index}].value">
                      <cm:include self="${entry.bean}" view="asLink"/>
                    </label>
                    <input type="hidden" name="<c:out value="${status.expression}"/>"
                    id="<c:out value="${status.expression}"/>"
                    value="<c:out value="${status.value}"/>"/>
                  </spring:bind>
                </li>
              </c:forEach>
            </ul>
          </div>
          <c:if test="${!pReadOnly}">
            <div class="fieldwrapper buttons">
              <fmt:message key="userDetails.interests.button.label" var="buttonLabel"/>
              <input type="submit" value="${buttonLabel}" class="button importantButton"/>
            </div>
          </c:if>
        </fieldset>
      </div>
    </form:form>
  </div>
</c:if>

