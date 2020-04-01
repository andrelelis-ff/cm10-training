package com.coremedia.livecontext.studio.desktop {
import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.desktop.TabTooltipInfo;
import com.coremedia.cms.editor.sdk.desktop.WorkAreaTab;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.preview.PreviewPanel;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.cms.editor.sdk.util.MessageBoxUtil;
import com.coremedia.ecommerce.studio.helper.AugmentationUtil;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.model.Catalog;
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ecommerce.studio.model.CatalogObjectPropertyNames;
import com.coremedia.ecommerce.studio.model.Category;
import com.coremedia.ecommerce.studio.model.Product;
import com.coremedia.ui.data.Locale;
import com.coremedia.ui.util.EncodingUtil;

import ext.Component;
import ext.Ext;
import ext.StringUtil;
import ext.container.Container;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.livecontext.studio.LivecontextStudioPlugin')]
public class CommerceWorkAreaTabBase extends WorkAreaTab {

  /**
   * The itemId of the whole document (left-hand side) container
   */
  public static const DOCUMENT_CONTAINER_ITEM_ID:String = "documentContainer";

  /**
   * The itemId of the movable splitter between form and preview
   */
  public static const PREVIEW_SPLIT_BAR_ITEM_ID:String = "previewSplitBar";

  public static const PREVIEW_PANEL_ITEM_ID:String = "previewPanel";

  private var documentPanelVisible:Boolean = true;
  private var previewVisible:Boolean = true;

  public function CommerceWorkAreaTabBase(config:CommerceWorkAreaTab = null) {
    config.entity = config.entity || config.object;
    super(config);

    var catalogObject:CatalogObject = getCatalogObject();

    catalogObject && catalogObject.load(function():void {
      replaceTab(false);
      catalogObject.addPropertyChangeListener(CatalogObjectPropertyNames.CONTENT, replaceTab);
    });

    catalogObject && catalogObject.addValueChangeListener(reloadPreview);
  }

  override public function calculateTitle():String {
    var catalogObject:CatalogObject = getCatalogObject();
    return catalogObject && CatalogHelper.getInstance().getDisplayName(catalogObject);
  }

  override public function calculateTooltip():TabTooltipInfo {
    var tabTooltipInfo:TabTooltipInfo = new TabTooltipInfo();

    var title:String = calculateTitle();
    if (!title) return undefined;
    tabTooltipInfo.addTooltipEntry(TabTooltipInfo.TITLE, null, title);

    var site:Site = editorContext.getSitesService().getSite(getCatalogObject().getSiteId());
    var siteName:String = site && site.getName();
    var siteLocale:Locale = site && site.getLocale();

    tabTooltipInfo.addTooltipEntry(TabTooltipInfo.SITE,
            resourceManager.getString('com.coremedia.cms.editor.Editor', 'WorkArea_Premular_tooltip_siteName'),
            siteName ? EncodingUtil.encodeForHTML(siteName) : resourceManager.getString('com.coremedia.cms.editor.Editor', 'WorkArea_Premular_tooltip_noSite'));

    tabTooltipInfo.addTooltipEntry(TabTooltipInfo.LOCALE,
            resourceManager.getString('com.coremedia.cms.editor.Editor', 'WorkArea_Premular_tooltip_locale'),
            siteLocale.getDisplayName());

    //add the catalog name
    var catalog:Catalog;
    if (getCatalogObject() is Category) {
      catalog = Category(getCatalogObject()).getCatalog();
    } else if (getCatalogObject() is Product) {
      catalog = Product(getCatalogObject()).getCatalog();
    }

    if (catalog) {
      var catalogName:String = catalog.getName();
      //if there is a catalog then wait until the name is available
      if (catalogName == undefined) {
        return undefined;
      }
      tabTooltipInfo.addTooltipEntry(CatalogObjectPropertyNames.CATALOG,
              resourceManager.getString('com.coremedia.livecontext.studio.LivecontextStudioPlugin', 'Commerce_catalog_label'),
              catalogName);
    }

    return tabTooltipInfo;
  }

  override public function calculateIcon():String {
    var catalogObject:CatalogObject = getCatalogObject();
    return catalogObject ? AugmentationUtil.getTypeCls(catalogObject) : super.calculateIcon();
  }

  /**
   * Handler for collapsing either the document panel or the preview panel.
   */
  protected function collapsePanel(itemId:String):void {
    handleCollapse(itemId);
  }

  internal function handleCollapse(itemId:String):void {
    switch (itemId) {
      case DOCUMENT_CONTAINER_ITEM_ID: {
        if (previewVisible) {
          documentPanelVisible = false;
        } else {
          previewVisible = true;
        }
        break;
      }
      case PREVIEW_PANEL_ITEM_ID: {
        if (documentPanelVisible) {
          previewVisible = false;
        } else {
          documentPanelVisible = true;
        }
        break;
      }
    }

    premularStateUpdated();

  }

  private function premularStateUpdated():void {

    if (rendered) {
      updateVisibility();
      updateLayout();
    }
  }

  private function updateVisibility():void {
    Ext.suspendLayouts();
    try {
      getDocumentContainer().setVisible(documentPanelVisible);
      getPreviewSplitBox().setVisible(documentPanelVisible && previewVisible);
      getPreviewPanel().setVisible(previewVisible);
    } finally {
      Ext.resumeLayouts();
    }
  }


  private function getDocumentContainer():Container {
    return Container(queryById(DOCUMENT_CONTAINER_ITEM_ID));
  }

  private function getPreviewPanel():PreviewPanel {
    return PreviewPanel(queryById(PREVIEW_PANEL_ITEM_ID));
  }

  private function getPreviewSplitBox():Component {
    return Component(queryById(PREVIEW_SPLIT_BAR_ITEM_ID));
  }

  private function getCatalogObject():CatalogObject {
    return getEntity() as CatalogObject;
  }

  private function replaceTab(showMessage:Boolean = true):void {
    var catalogObject:CatalogObject = getCatalogObject();
    var augmentingContent:Content = catalogObject.get(CatalogObjectPropertyNames.CONTENT) as Content;
    if (augmentingContent) { // the commerce object has been augmented
      editorContext.getWorkAreaTabManager().replaceTab(catalogObject, augmentingContent);
      if (destroyed && showMessage) { //show the message only for the already rendered and then destroyed tabs
        augmentingContent.load(function ():void {
          if (augmentingContent.getCreator() !== SESSION.getUser()) { //don't show the message if the category is augmented by myself.
            showAugmentationMessage(catalogObject);
          }
        });
      }
    }
  }

  private static function showAugmentationMessage(augmentedCatalogObject:CatalogObject):void {
    var title:String = ResourceManager.getInstance().getString('com.coremedia.livecontext.studio.LivecontextStudioPlugin', 'Category_augmentedMessage_title');
    var categoryName:String = CatalogHelper.getInstance().getDisplayName(augmentedCatalogObject);
    var text:String = StringUtil.format(ResourceManager.getInstance().getString(
            'com.coremedia.livecontext.studio.LivecontextStudioPlugin', 'Category_augmentedMessage_text'), categoryName);

    MessageBoxUtil.showInfo(title, text);
  }

  private function reloadPreview():void {
    var previewPanel:PreviewPanel = getComponent(PREVIEW_PANEL_ITEM_ID) as PreviewPanel;
    //TODO: the preview panel cannot be found sometimes
    previewPanel && previewPanel.reloadFrame();
  }


  override protected function onDestroy():void {
    getCatalogObject().removePropertyChangeListener(CatalogObjectPropertyNames.CONTENT, replaceTab);
    getCatalogObject().removeValueChangeListener(reloadPreview);
    super.onDestroy();
  }

}
}