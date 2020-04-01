package com.coremedia.catalog.studio.library {
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.util.MessageBoxUtilInternal;

import ext.StringUtil;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.cms.editor.Editor')]
[ResourceBundle('com.coremedia.catalog.studio.CatalogStudioPlugin')]
public class CatalogCopy {
  private var treeRelation:CatalogTreeRelation;
  private var sources:Array;
  private var newParent:Content;
  private var callback:Function;

  public function CatalogCopy(catalogTreeRelation:CatalogTreeRelation, sources:Array, newParent:Content, callback:Function = undefined) {
    this.treeRelation = catalogTreeRelation;
    this.sources = sources;
    this.newParent = newParent;
    this.callback = callback;
  }

  public function execute():void {
    //TODO we only copy products here! see mayCopy check
    var parents:Array = treeRelation.getParents(sources[0]);

    if (!callback) {
      callback = function () {
      };
    }

    // Create a "real" product copy when we stay in the same folder
    if (parents.indexOf(newParent) !== -1) {
      CatalogTreeRelationHelper.copyAndLinkProducts(sources, newParent, callback);
    }
    //the products are copied into in another category, so we ask the user what to do: create a copy or the linking?
    else {
      var msg:String = StringUtil.format(ResourceManager.getInstance().getString('com.coremedia.catalog.studio.CatalogStudioPlugin', 'catalog_copy_or_link_message'), newParent.getName());
      MessageBoxUtilInternal.show(ResourceManager.getInstance().getString('com.coremedia.catalog.studio.CatalogStudioPlugin', 'catalog_copy_or_link_title'), msg, null, {
                yes: ResourceManager.getInstance().getString('com.coremedia.catalog.studio.CatalogStudioPlugin', 'catalog_copy_btn_text'),
                no: ResourceManager.getInstance().getString('com.coremedia.catalog.studio.CatalogStudioPlugin', 'catalog_link_btn_text'),
                cancel: ResourceManager.getInstance().getString('com.coremedia.cms.editor.Editor', 'dialog_defaultCancelButton_text')
              },
              function (btn:String):void {
                if (btn === 'cancel') {
                  return;
                }

                var copy:Boolean = (btn === 'yes');
                if (copy) {
                  CatalogTreeRelationHelper.copyAndLinkProducts(sources, newParent, callback);
                }
                else {
                  treeRelation.addChildNodes(newParent, sources, callback);
                }
              }
      );
    }
  }

}
}