package com.coremedia.ecommerce.studio.dragdrop {
import com.coremedia.ecommerce.studio.helper.AugmentationUtil;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ui.util.DraggableItemsUtils;

import ext.StringUtil;
import ext.Template;
import ext.XTemplate;

import mx.resources.ResourceManager;

/**
 * A helper class to create drag and drop visual feedback HTML
 */
[ResourceBundle('com.coremedia.ecommerce.studio.ECommerceStudioPlugin')]
public class CatalogDragDropVisualFeedback {

  private static var simpleDragDropTemplate:Template = new XTemplate(
    '<span>{text:htmlEncode}</span>').compile();

  public static function getHtmlFeedback(items:Array) : String {
    if (!items || items.length === 0) {
      return null;
    }

    if (items.length === 1) {
      //the item can be a CatalogObject or a BeanRecord
      var catalogObject:CatalogObject = (items[0] is CatalogObject)? items[0] : items[0].getBean();
      return DraggableItemsUtils.DRAG_GHOST_TEMPLATE.apply({
        title : CatalogHelper.getInstance().getDecoratedName(catalogObject),
        icon : AugmentationUtil.getTypeCls(catalogObject)
      });
    } else {
      return simpleDragDropTemplate.apply({
        text : StringUtil.format(ResourceManager.getInstance().getString('com.coremedia.ecommerce.studio.ECommerceStudioPlugin', 'Catalog_DragDrop_multiSelect_text'), items.length)
      });
    }
  }
}
}
