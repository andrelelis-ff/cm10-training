package com.coremedia.blueprint.studio.taxonomy.rendering {
import ext.Component;
import ext.Ext;

import js.Event;
import js.HTMLElement;
import js.NodeList;

public class NodePathEllipsis {
  private var wrapperId:String;
  private var componentId:String;
  private var nodes:Array;
  private var averageNodeWith:Number;
  private var scrollable:Boolean;
  private var fixWidth:Number;

  public function NodePathEllipsis(wrapperId:String, renderer:TaxonomyRenderer, fixWidth:Number = undefined) {
    this.wrapperId = wrapperId;
    this.componentId = renderer.componentId;
    this.nodes = renderer.nodes;
    this.fixWidth = fixWidth;
    this.scrollable = renderer.isScrollable();
  }

  /**
   * This is executed once the rendering is done.
   * We can calculate now all the actual width's, so no more guessing
   * what the width of a node might be.
   */
  public function autoEllipsis():void {

    // Try to add the resize listener
    addResizeListener();

    var actualTotalWidth:Number = 0;
    var spanElement:* = window.document.getElementById(wrapperId);
    //might not be there if not match was found
    if (!spanElement) {
      return;
    }

    var nodeElements:Array = spanElement.childNodes;

    var nodeWidthMap:* = {};

    // Cache the node withs and calculate the totalWidth of all nodes
    for (var i:int = 0; i < nodeElements.length; i++) {
      var c:HTMLElement = nodeElements[i];
      disableEllipsis(c);
      nodeWidthMap[i] = getNodeWidth(c);
      actualTotalWidth += nodeWidthMap[i];
    }

    var componentWidth:Number = getComponentWidth();
    //check if a scrollbar may overlap
    if (scrollable) {
      componentWidth = componentWidth - 32;
    }

    // Should nodes be ellipsified?
    if (actualTotalWidth <= componentWidth) {
      //the path rendering fits into the container, so everything is fine
      for (var j:int = 1; j < nodeElements.length - 1; j++) {
        var node = nodeElements[j];
        removeNodeMouseHandlers(node);
      }
    }
    else {
      //Apply stripping: calculate the average size, but let the leaf AND root as it is
      var availableParentWidth:Number = componentWidth - nodeWidthMap[nodeElements.length - 1] - nodeWidthMap[0];
      if (availableParentWidth > 0) {
        averageNodeWith = Math.floor(availableParentWidth / (nodes.length - 2));

        var nodePadding:Number = getNodePadding(nodeElements[nodeElements.length - 1]);

        //...and apply the value
        for (var k:int = 1; k < nodeElements.length - 1; k++) {
          node = nodeElements[k];
          var nodeWidth:Number = nodeWidthMap[k];
          var surroundingWidth:Number = nodeWidth - getNodeBoxWidth(node) + nodePadding;
          if (nodeWidth > averageNodeWith && nodeWidth > surroundingWidth) {
            //store the width as data attribute since we have to access it in a static context
            getNameNode(node).setAttribute("data-width", averageNodeWith - surroundingWidth);
            addNodeMouseEventHandlers(node);
          }
          else {
            //not stripped, given node is short enough
          }
        }
      }
    }
  }

  /**
   * The first node after a break caused by the drop down width has the full length: ~680px
   * As a workaround we calculate the width of this node.
   */
  private function getNodeWidth(node:HTMLElement):Number {
    return Ext.fly(node).getWidth();
  }

  /**
   * Calculate the width of cm-taxonomy-node__box
   *
   * @param node
   * @return
   */
  private function getNodeBoxWidth(node:HTMLElement):Number {
    var elements:Array = Ext.fly(node).query(TaxonomyBEMEntities.NODE_ELEMENT_BOX.getCSSSelector(), false) as Array;
    return elements && elements.length > 0 ? elements[0].getWidth() : 0;
  }

  /**
   * Get the padding of cm-taxonomy-node
   *
   * @param node
   * @return
   */
  private function getNodePadding(node:HTMLElement):Number {
    return getNodeWidth(node) - getNodeBoxWidth(node);
  }

  /**
   * Adds the hover listeners for those nodes that are ellipsed.
   * Note that this method must be static so that the listeners
   * and be registered and de-registered on refresh.
   * @param node the node element to apply the ellipsis for
   */
  private function addNodeMouseEventHandlers(node:HTMLElement):void {
    node.removeEventListener('mouseover', handleNodeMouseOver, false);
    node.addEventListener('mouseover', handleNodeMouseOver, false);
    enableEllipsis(node);

    node.removeEventListener('mouseout', handleNodeMouseOut, false);
    node.addEventListener('mouseout', handleNodeMouseOut, false);
  }

  /**
   * Removes the hover listeners for those nodes that are ellipsed.
   * @param node the node element to remove the ellipsis from
   */
  private function removeNodeMouseHandlers(node:HTMLElement):void {
    node.removeEventListener('mouseover', handleNodeMouseOver, false);
    disableEllipsis(node);

    node.removeEventListener('mouseout', handleNodeMouseOut, false);
  }

  /**
   * The mouse over listener that is executed for ellipsed nodes.
   */
  private static function handleNodeMouseOver(event:Event):void {
    disableEllipsis(event.target as HTMLElement)
  }

  /**
   * The mouse out listener that is executed for ellipsed nodes.
   */
  private static function handleNodeMouseOut(event:Event):void {
    enableEllipsis(event.target as HTMLElement);
  }

  /**
   * Enable ellisifying of nodes
   *
   * @param node
   */
  private static function enableEllipsis(node:HTMLElement):void {
    var nodeSpan:HTMLElement = getTaxonomyNode(node);
    var textSpan:HTMLElement = getNameNode(node);
    var width:String = Ext.fly(textSpan).getAttribute("data-width");
    Ext.fly(nodeSpan).addCls(TaxonomyBEMEntities.NODE_MODIFIER_ELLIPSIS.getCSSClass());
    Ext.fly(textSpan).setStyle("width", width + "px");
  }

  /**
   * Disable ellisifying of nodes
   *
   * @param node
   */
  private static function disableEllipsis(node:HTMLElement):void {
    var nodeSpan:HTMLElement = getTaxonomyNode(node);
    var textSpan:HTMLElement = getNameNode(node);
    Ext.fly(textSpan).setStyle("width");
    Ext.fly(nodeSpan).removeCls(TaxonomyBEMEntities.NODE_MODIFIER_ELLIPSIS.getCSSClass());
  }

  /**
   * Get the cm-taxonomy-node__name span
   *
   * @param selection
   * @return
   */
  private static function getNameNode(selection:*):* {
    var textSpan:* = selection.getElementsByClassName(TaxonomyBEMEntities.NODE_ELEMENT_NAME)[0];
    if (!textSpan) {
      textSpan = selection;
    }
    return textSpan;
  }

  /**
   * Get the cm-taxonomy-node span
   *
   * @param selection
   * @return
   */
  private static function getTaxonomyNode(selection:*):* {
    return Ext.fly(selection).up(TaxonomyBEMEntities.NODE_BLOCK.getCSSSelector()) || selection;
  }

  /**
   * Called when the taxonomy leaf is not fully visible. Previous elements are hidden then.
   */
  function leafMouseOver():void {
    var componentWidth:Number = getComponentWidth();
    var spanElement:HTMLElement = HTMLElement(window.document.getElementById(wrapperId));
    var nodeElements:NodeList = spanElement.childNodes;
    var actualTotalWidth:Number = 0;
    //calculate current with...
    for (var i:uint = 0; i < nodeElements.length; i++) {
      actualTotalWidth += nodeElements[i].offsetWidth;
    }
    actualTotalWidth = actualTotalWidth + 10; //add margin left and right
    if (actualTotalWidth > componentWidth) {
      var margin:Number = actualTotalWidth - componentWidth + 25;
      if (spanElement.parentNode) {
        spanElement.parentNode.style.marginLeft = '-' + margin + 'px';
      }
    }
  }

  /**
   * Called when the leaf of a link list taxonomy has been moved
   * to the left, so that it is fully visible. On the mouse out event, the previous
   * elements are shown again.
   */
  function leafMouseOut():void {
    var spanElement:HTMLElement = HTMLElement(window.document.getElementById(wrapperId));
    if (spanElement && spanElement.parentNode) {
      spanElement.parentNode.style.marginLeft = '0px';
    }
  }

  /**
   * Calculates the available with for component the path should be rendered into.
   * @return The available pixels.
   */
  private function getComponentWidth():Number {
    if (fixWidth) {
      return fixWidth;
    }
    return Ext.get(this.componentId).getWidth();
  }

  /**
   * Add a resize listener to the component, if the component has no fix width.
   */
  private function addResizeListener():void {
    if (!fixWidth) {
      var component:Component = Ext.getCmp(this.componentId);
      component.removeListener("resize", autoEllipsis);
      component.addListener("resize", autoEllipsis);
    }
  }
}
}