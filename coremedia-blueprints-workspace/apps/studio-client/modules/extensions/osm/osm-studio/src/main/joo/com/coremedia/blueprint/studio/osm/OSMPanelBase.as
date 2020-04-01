package com.coremedia.blueprint.studio.osm {

import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.premular.TabbedDocumentFormDispatcher;
import com.coremedia.ui.data.ValueExpression;

import ext.Ext;
import ext.panel.Panel;

/**
 * The open streetmap panel implementation, including marker moving
 * and position updating.
 *
 * Note that the implementation is based on the OpenLayers API.
 */
public class OSMPanelBase extends Panel {
  private static const MARKER_Z_INDEX:int = 1000;

  private var zoom:int = 5;

  private var latLngExpression:ValueExpression;

  private var map;
  private var marker:*;
  private var vectorLayer:*;
  private var skipCalcZoomLevel:Boolean = false;

  public function OSMPanelBase(config:OSMPanel = null) {
    super(config);
    this.latLngExpression = config.latLngExpression;
    addListener('afterlayout', initMap);
    calcZoomLevel();
  }

  /**
   * Fired after the panel has been resized.
   */
  private function resized():void {
    map.updateSize();
  }

  private function calcZoomLevel():void {
    //set by the taxonomy manager
    var level:int = editorContext.getApplicationContext().get('taxonomy_node_level');
    if(level && level <= 1) {
      zoom = (level+2);
    }
    else if(level && level <= 2) {
      zoom = (level+3);
    }
    else if(level && level <= 3) {
      zoom = (level+4);
    }
    else if(level && level <= 5) {
      zoom = (level+5);
    }
    else {
      zoom = 11;
    }
  }

  /**
   * Initialize the map and add it to the dom.
   */
  private function initMap():void {
    removeListener('afterlayout', initMap);

    var dispatcher:TabbedDocumentFormDispatcher = this.findParentByType(TabbedDocumentFormDispatcher) as TabbedDocumentFormDispatcher;
    if(dispatcher) {
      dispatcher.addListener('resize', resized);
    }

    latLngExpression.loadValue(function ():void {
      createMap();
      createMarkerLayer();
      createDragFeature();
      setMarker();

      // Listen to changes on latitude/longitude expression
      latLngExpression.addChangeListener(setMarker);
    });
  }


  /**
   * Creates the map and its navigation controls.
   */
  private function createMap():void {
    //determine the panel element where the map should be added, (could be improved via findBy?)
    var mapId = body.getAttribute("id"); //nothing special, just the generated ExtJs id.
    map = new OpenLayers.Map(mapId, {
      autoUpdateSize: true,
      projection:new OpenLayers.Projection("EPSG:900913"),
      displayProjection:new OpenLayers.Projection("EPSG:4326"),
      controls:[
        new OpenLayers['Control'].Navigation({documentDrag:true}),
        new OpenLayers['Control'].PanZoomBar()],
      maxExtent:new OpenLayers.Bounds(-20037508.34, -20037508.34,
              20037508.34, 20037508.34),
      numZoomLevels:10,
      maxResolution:156543,
      units:'meters'
    });

    //adds the actual map layer to the map. enable the layer switcher control to see which layers are added to the map.
    var osmLayer = new OpenLayers['Layer'].OSM();
    map.addLayer(osmLayer);
  }


  /**
   * OpenLayers (as the name implies) uses different layers to control the map.
   * We add a vector layer here so that geometric figures like 'points' can be drawn on it.
   * Additionally, the layer is styled afterwards, in this case we use the base64 encoded
   * marker as graphic for the layer.
   */
  private function createMarkerLayer():void {
    var renderer = OpenLayers['Util'].getParameters(window.location.href).renderer;
    renderer = (renderer) ? [renderer] : OpenLayers['Layer'].Vector.prototype['renderers'];

    vectorLayer = new OpenLayers['Layer'].Vector("Marker Drop Shadows", {
      styleMap:new OpenLayers['StyleMap']({
        // Set the external graphic and background graphic images.
        externalGraphic:getMarker(),
        graphicYOffset:-25,

        // Set the z-indexes of both graphics to make sure the background
        // graphics stay in the background (shadows on top of markers looks
        // odd; let's not do that).
        graphicZIndex:MARKER_Z_INDEX,
        pointRadius:12 //marker height
      }),
      isBaseLayer:false,
      rendererOptions:{yOrdering:true},
      renderers:renderer
    });

    //add the layer to the map. enable the layer switcher control to see which layers are added to the map.
    map.addLayer(vectorLayer);
  }

  /**
   * The drag and drop feature is used to support
   * the point dragging on the vector layer.
   * There is no "real" marker here, but a geometric point instead with the marker layout.
   */
  private function createDragFeature():void {
    var modifyFeaturesControl = new OpenLayers['Control'].ModifyFeature(vectorLayer);
    modifyFeaturesControl.mode = OpenLayers['Control'].ModifyFeature.RESHAPE;
    map.addControl(modifyFeaturesControl);
    modifyFeaturesControl.activate();

    // Add a drag feature control to move features around.
    var dragFeature = new OpenLayers['Control'].DragFeature(vectorLayer, {
      autoActivate:true,
      onComplete:function (layer, xy) {
        var px = new OpenLayers['Pixel'](xy.x, xy.y + 16); //TODO mmmh, somehow more precisely?

        //we have to transform the coordinates back to the system that is used in the blueprint.
        var lonLat = map.getLonLatFromPixel(px).transform(
                map.getProjectionObject(), // transform from WGS 1984
                new OpenLayers.Projection("EPSG:4326"));

        var newLatLng:String = lonLat.lat + "," + lonLat.lon;

        //remember current zoom level
        zoom = map.getZoom();
        editorContext.getApplicationContext().set('taxonomy_node_level', zoom);
        skipCalcZoomLevel = true;
        latLngExpression.setValue(newLatLng);
      }
    });
    map.addControl(dragFeature);
    dragFeature.activate();
  }

  /**
   * Initial setup of the marker.
   * Will create and place the marker, set the zoom level and center the map.
   */
  private function setMarker():void {
    if(latLngExpression.getValue()) {
      var lon = getLongitude();
      var lat = getLatitude();
      var lonLat:* = getLatLon(lat, lon);

      //skip zoom change if the marker was moved!
      if(!skipCalcZoomLevel) {
        calcZoomLevel();
      }
      skipCalcZoomLevel = false;

      if(marker) {
        vectorLayer.removeAllFeatures();
      }
      marker = new OpenLayers['Feature'].Vector(new OpenLayers['Geometry'].Point(lonLat.lon, lonLat.lat));
      map.zoomIn(zoom);
      vectorLayer.addFeatures([marker]);
      map.setCenter(lonLat, zoom);
    }
  }

  /**
   * Returns the latitude value.
   * @return
   */
  private function getLatitude():Number {
    var latLngArray:Array = latLngExpression.getValue().split(',');
    var latitude:Number = Number(latLngArray[0]);
    return latitude;
  }

  /**
   * Returns the longitude value.
   * @return
   */
  private function getLongitude():Number {
    var latLngArray:Array = latLngExpression.getValue().split(',');
    var longitude:Number = Number(latLngArray[1]);
    return longitude;
  }

  /**
   * Resolves the marker that is used to show the lat/long value.
   */
  private function getMarker():String {
    var groupId:String = resourceManager.getString('com.coremedia.blueprint.studio.osm.OSMStudioPlugin', 'osm.groupId');
    if(groupId && groupId.length > 0) {
      var markerUrl:String = Ext.getResourcePath('osm/img/marker.png', null, groupId);
      if(markerUrl) {
        return markerUrl;
      }
    }

    return resourceManager.getString('com.coremedia.blueprint.studio.osm.OSMStudioPlugin', 'osm.marker');
  }

  /**
   * Returns the current latitude and longitude.
   * @return
   */
  private function getLatLon(lat:*, lon:*):* {
    var latLng:* = new OpenLayers['LonLat'](lon, lat)
            .transform(
            new OpenLayers.Projection("EPSG:4326"), // transform from WGS 1984
            map.getProjectionObject() // to Spherical Mercator Projection
    );
    return latLng;
  }
}
}
