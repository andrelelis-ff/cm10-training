package com.coremedia.livecontext.studio.pbe {
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.preview.MetadataHelper;
import com.coremedia.cms.editor.sdk.preview.metadata.ContentMetadataNodeRenderer;
import com.coremedia.cms.editor.sdk.preview.metadata.MetadataNodeRenderer;
import com.coremedia.cms.editor.sdk.preview.metadata.MetadataTreeNode;
import com.coremedia.ecommerce.studio.model.Store;
import com.coremedia.ui.data.RemoteBean;

import ext.Ext;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.livecontext.studio.LivecontextStudioPlugin')]
public class StoreNodeRenderer implements MetadataNodeRenderer {
  private static const PROPERTY_NODE_ICON_CLASS:String = ResourceManager.getInstance().getString('com.coremedia.icons.CoreIcons', 'arrow_right');

  public function StoreNodeRenderer() {
    super();
  }

  public function canRender(metadataNode:MetadataTreeNode):Boolean {
    // this one cares for store node and first property node.
    return MetadataHelper.getBeanMetadataValue(metadataNode) is Store || MetadataHelper.getBeanMetadataValue(metadataNode.getParent()) is Store;
  }

  public function renderText(metadataNode:MetadataTreeNode):String {
    if (MetadataHelper.isPropertyMetadataNode(metadataNode)){
      return ResourceManager.getInstance().getString('com.coremedia.livecontext.studio.LivecontextStudioPlugin', 'Commerce_shopUrl');
    }
    var store:Store = Store(MetadataHelper.getBeanMetadataValue(metadataNode));

    var children:Array = metadataNode.getChildren();
    if(!Ext.isEmpty(children)) {
      var properties:Object = MetadataHelper.getAllProperties(children[0]);
      if(properties.shopUrl) {
        var remoteBean:RemoteBean = store.resolveShopUrlForPbe(properties.shopUrl);
        if(undefined === remoteBean) {
          return undefined;
        } else if (remoteBean is Content) {
          return ContentMetadataNodeRenderer.renderTextForContent(Content(remoteBean));
        } else if (undefined !== properties.pageId) {
          return properties.pageId;
        }
      }
    }

    return store.getName();
  }

  public function renderIconCls(metadataNode:MetadataTreeNode):String {
    if (MetadataHelper.isPropertyMetadataNode(metadataNode)){
      return PROPERTY_NODE_ICON_CLASS;
    }
    var children:Array = metadataNode.getChildren();
    if(!Ext.isEmpty(children)) {
      var properties:Object = MetadataHelper.getAllProperties(children[0]);
      var store:Store = Store(MetadataHelper.getBeanMetadataValue(metadataNode));
      if(properties.shopUrl) {
        var remoteBean:RemoteBean = store.resolveShopUrlForPbe(properties.shopUrl);
        if(undefined === remoteBean) {
          return undefined;
        } else if (remoteBean is Content) {
          return ContentMetadataNodeRenderer.renderIconClsForContent(Content(remoteBean));
        }
      }
    }
    return ResourceManager.getInstance().getString('com.coremedia.livecontext.studio.LivecontextStudioPlugin', 'CMExternalPage_icon');
  }
}
}