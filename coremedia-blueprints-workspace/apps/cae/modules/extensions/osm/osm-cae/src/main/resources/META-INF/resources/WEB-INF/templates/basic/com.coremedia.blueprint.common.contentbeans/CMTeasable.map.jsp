<%@ page contentType="text/html; charset=UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="bp" uri="http://www.coremedia.com/2012/blueprint" %>
<%@ include file="/WEB-INF/includes/taglibs.jinc" %>
<%--@elvariable id="self" type="com.coremedia.blueprint.common.contentbeans.CMTeasable"--%>
<c:set var="locationTaxonomy" value="${self.locationTaxonomy}"/>
<c:if test="${not empty locationTaxonomy}">
  <div class="mapBox box">
    <h3>
      <fmt:message key="map.box.title"/>
    </h3>

    <div class="content">

      <c:set var="zoom" value="${bp:settingWithDefault(self, 'map.zoom', 6)}"/>
      <div id="map_${self.contentId}" class="mapholder"></div>
      <script type="text/javascript">

        var map;

        <%-- Position und Zoomstufe der Karte --%>
        var lon = ${locationTaxonomy[0].longitude};
        var lat = ${locationTaxonomy[0].latitude};
        var zoom = ${zoom};

        map = new OpenLayers.Map('map_${self.contentId}');
        map.addLayer(new OpenLayers.Layer.OSM());
        var lonLat = new OpenLayers.LonLat(lon, lat)
                .transform(
                new OpenLayers.Projection("EPSG:4326"), // transform from WGS 1984
                map.getProjectionObject() // to Spherical Mercator Projection
        );
        var markers = new OpenLayers.Layer.Markers( "Markers" );
        map.addLayer(markers);

        var marker = new OpenLayers.Marker(lonLat);
        marker.icon = new OpenLayers.Icon("<c:url value='/osm/img/marker.png'/>");
        markers.addMarker(marker);

        map.setCenter(lonLat, zoom);


      </script>
    </div>
  </div>
</c:if>
