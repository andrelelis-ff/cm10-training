package com.coremedia.livecontext.ibm.studio.mgmtcenter {

import ext.Component;
import ext.Ext;
import ext.dom.Element;

import js.Window;

[ResourceBundle('com.coremedia.livecontext.studio.LivecontextStudioPlugin')]
public class ManagementCenterFrameBase extends Component {

  public function ManagementCenterFrameBase(config:ManagementCenterFrame = null) {
    super(ManagementCenterFrame(Ext.apply({
      layout:'fit',
      title:resourceManager.getString('com.coremedia.livecontext.studio.LivecontextStudioPlugin', 'Window_ManagementCenter_title')
    }, config)));
  }

  private function setUrl():void {
    var url:String = ManagementCenterUtil.getUrl();
    var elem:Element = getEl();
    if (elem) {
      elem.set({src: url});

      // only IE doesn't reload the iframe now, so we have to enforce it:
      if (Ext.isIE) {
        var contentWindow:Window = getContentWindow();
        if(contentWindow) {
          contentWindow.location.href = url; // necessary for IE only!
        }
      }
    }
  }

  protected override function onRender(parentNode:Element, containerIdx:Number):void {
    super.onRender(parentNode, containerIdx);
    setUrl();
    this['el'] = parentNode.createChild({
      tag:'iframe',
      id:'iframe-' + this.getId(),
      frameBorder:0,
      width:"100%",
      height:"100%",
      src:ManagementCenterUtil.getUrl()
    });
  }

  public function getContentWindow():Window{
    return (this.el && this.el.dom) ? this.el.dom.contentWindow : null;
  }

}
}