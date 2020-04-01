<%@ page contentType="text/html; charset=UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--@elvariable id="self" type="com.coremedia.blueprint.osm.OpenStreetMapAspect"--%>
<c:if test="${self.enabled}">
  <script type="text/javascript" src="<c:url value='/osm/OpenLayers.js'/>"></script>
  <script type="text/javascript" src="<c:url value='/osm/OpenStreetMap.js'/>"></script>
</c:if>