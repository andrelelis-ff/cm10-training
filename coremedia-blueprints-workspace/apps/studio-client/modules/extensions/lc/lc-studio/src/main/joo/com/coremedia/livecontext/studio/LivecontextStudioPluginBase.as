package com.coremedia.livecontext.studio {

import com.coremedia.blueprint.studio.CMChannelExtension;
import com.coremedia.blueprint.studio.util.ContentInitializer;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentProperties;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructType;
import com.coremedia.cms.editor.configuration.StudioPlugin;
import com.coremedia.cms.editor.sdk.EditorContextImpl;
import com.coremedia.cms.editor.sdk.IEditorContext;
import com.coremedia.cms.editor.sdk.desktop.TabTooltipEntry;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.preview.PreviewPanel;
import com.coremedia.cms.editor.sdk.preview.PreviewURI;
import com.coremedia.cms.editor.sdk.quickcreate.QuickCreate;
import com.coremedia.cms.editor.sdk.quickcreate.processing.ProcessingData;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.cms.editor.sdk.util.MetaStyleService;
import com.coremedia.cms.editor.sdk.util.ThumbnailResolverFactory;
import com.coremedia.ecommerce.studio.CatalogModel;
import com.coremedia.ecommerce.studio.augmentation.augmentationService;
import com.coremedia.ecommerce.studio.components.link.CatalogLinkPropertyField;
import com.coremedia.ecommerce.studio.helper.AugmentationUtil;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.helper.StoreUtil;
import com.coremedia.ecommerce.studio.model.Catalog;
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ecommerce.studio.model.CatalogObjectPropertyNames;
import com.coremedia.ecommerce.studio.model.Category;
import com.coremedia.ecommerce.studio.model.Product;
import com.coremedia.ecommerce.studio.model.Store;
import com.coremedia.livecontext.studio.library.LivecontextCollectionViewExtension;
import com.coremedia.livecontext.studio.library.ShowInCatalogTreeHelper;
import com.coremedia.livecontext.studio.pbe.StoreNodeRenderer;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.util.UrlUtil;

import ext.Component;
import ext.ComponentManager;
import ext.form.FieldContainer;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.livecontext.studio.LivecontextStudioPlugin')]
public class LivecontextStudioPluginBase extends StudioPlugin {

  internal static const CONTENT_LED_PROPERTY:String = 'livecontext.contentLed';
  internal static const MANAGE_NAVIGATION_PROPERTY:String = 'livecontext.manageNavigation';
  internal static const EXTERNAL_ID_PROPERTY:String = 'externalId';

  internal static const LOCAL_SETTINGS_STRUCT_NAME:String = 'localSettings';
  internal static const PRODUCT_LIST_STRUCT_NAME:String = 'productList';
  internal static const OFFSET_NAME:String = 'offset';
  internal static const MAX_LENGTH_NAME:String = 'maxLength';

  public static const VENDOR_IBM:String = "IBM";
  public static const VENDOR_CM:String = "coremedia";
  public static const VENDOR_HYBRIS:String = "SAP Hybris";
  public static const VENDOR_SFCC:String = "Salesforce";

  public function LivecontextStudioPluginBase(config:LivecontextStudioPlugin = null) {
    if (UrlUtil.getHashParam('livecontext') === 'false') {
      delete config['rules'];
      delete config['configuration'];
    }
    super(config);
  }


  override public function init(editorContext:IEditorContext):void {
    super.init(editorContext);

    editorContext.getCollectionViewExtender().addExtension(new LivecontextCollectionViewExtension());

    //forward the workspaceId (configured by the hash param) to the preview url.
    editorContext.registerPreviewUrlTransformer(function (uri:PreviewURI, callback:Function):void {
      var workspaceId:String = CatalogHelper.getInstance().getExtractedWorkspaceId();
      if (workspaceId && workspaceId !== CatalogHelper.NO_WS) {
        uri.appendParameter("workspaceId", workspaceId);
      }
      callback.call(null);
    });

    /**
     * Apply image link list preview
     */
    editorContext.registerThumbnailResolver(new CatalogThumbnailResolver(LivecontextStudioPlugin.CONTENT_TYPE_EXTERNAL_CHANNEL));
    editorContext.registerThumbnailResolver(new CatalogThumbnailResolver(LivecontextStudioPlugin.CONTENT_TYPE_EXTERNAL_PRODUCT));
    editorContext.registerThumbnailResolver(ThumbnailResolverFactory.create(LivecontextStudioPlugin.CONTENT_TYPE_EXTERNAL_PAGE, "pictures"));
    editorContext.registerThumbnailResolver(new CatalogThumbnailResolver(LivecontextStudioPlugin.CONTENT_TYPE_MARKETING_SPOT));
    editorContext.registerThumbnailResolver(new CatalogThumbnailResolver("CatalogObject"));
    editorContext.registerThumbnailResolver(new CatalogTeaserThumbnailResolver(LivecontextStudioPlugin.CONTENT_TYPE_PRODUCT_TEASER));
    editorContext.registerThumbnailResolver(ThumbnailResolverFactory.create(LivecontextStudioPlugin.CONTENT_TYPE_PRODUCT_TEASER, "pictures"));


    editorContext.registerThumbnailResolver(new CatalogThumbnailResolver(CatalogModel.TYPE_CATEGORY));
    editorContext.registerThumbnailResolver(new CatalogThumbnailResolver(CatalogModel.TYPE_MARKETING));
    editorContext.registerThumbnailResolver(new CatalogThumbnailResolver(CatalogModel.TYPE_MARKETING_SPOT));
    editorContext.registerThumbnailResolver(new CatalogThumbnailResolver(CatalogModel.TYPE_PRODUCT));
    editorContext.registerThumbnailResolver(new CatalogThumbnailResolver(CatalogModel.TYPE_PRODUCT_VARIANT));

    /**
     * Register Content initializer
     */
    editorContext.registerContentInitializer(LivecontextStudioPlugin.CONTENT_TYPE_MARKETING_SPOT, initMarketingSpot);
    editorContext.registerContentInitializer(LivecontextStudioPlugin.CONTENT_TYPE_PRODUCT_LIST, initProductList);
    editorContext.registerContentInitializer(LivecontextStudioPlugin.CONTENT_TYPE_PRODUCT_TEASER, initProductTeaser);
    editorContext.registerContentInitializer(LivecontextStudioPlugin.CONTENT_TYPE_EXTERNAL_CHANNEL, initExternalChannel);
    editorContext.registerContentInitializer(LivecontextStudioPlugin.CONTENT_TYPE_EXTERNAL_PRODUCT, initExternalProduct);
    editorContext.registerContentInitializer(LivecontextStudioPlugin.CONTENT_TYPE_EXTERNAL_PAGE, initExternalPage);

    editorContext['getMetadataNodeRendererRegistry']().register(new StoreNodeRenderer());

    /**
     * Extend Content initializer
     */
    editorContext.extendContentInitializer(LivecontextStudioPlugin.CONTENT_TYPE_IMAGE_MAP, extendImageMap);

    /**
     * apply the marketing spot link field to CMMarketingSpot quick create dialog
     */
    QuickCreate.addQuickCreateDialogProperty(LivecontextStudioPlugin.CONTENT_TYPE_MARKETING_SPOT, EXTERNAL_ID_PROPERTY,
            function (data:ProcessingData, properties:Object):Component {
              var config:CatalogLinkPropertyField = CatalogLinkPropertyField(properties);
              config.dropAreaHandler = CatalogHelper.getInstance().openMarketingSpots;
              config.maxCardinality = 1;
              config.replaceOnDrop = true;
              config.linkTypeNames = [CatalogModel.TYPE_MARKETING_SPOT];
              config.dropAreaText = ResourceManager.getInstance().getString('com.coremedia.livecontext.studio.LivecontextStudioPlugin', 'MarketingSpot_Link_empty_text');
              config.hideRemove = true;
              var myCatalogLink:CatalogLinkPropertyField = new CatalogLinkPropertyField(config);
              var containerCfg:FieldContainer = FieldContainer({});
              containerCfg.fieldLabel = properties.label;
              containerCfg.items = [myCatalogLink];
              return ComponentManager.create(containerCfg);
            });


    /**
     * apply the product link field to CMProductTeaser quick create dialog
     */
    QuickCreate.addQuickCreateDialogProperty(LivecontextStudioPlugin.CONTENT_TYPE_PRODUCT_TEASER, EXTERNAL_ID_PROPERTY,
            function (data:ProcessingData, properties:Object):Component {
              var config:CatalogLinkPropertyField = CatalogLinkPropertyField(properties);
              config.maxCardinality = 1;
              config.replaceOnDrop = true;
              config.linkTypeNames = [CatalogModel.TYPE_PRODUCT];
              config.dropAreaText = ResourceManager.getInstance().getString('com.coremedia.livecontext.studio.LivecontextStudioPlugin', 'Product_Link_empty_text');
              config.hideRemove = true;
              var myCatalogLink:CatalogLinkPropertyField = new CatalogLinkPropertyField(config);
              var containerCfg:FieldContainer = FieldContainer({});
              containerCfg.fieldLabel = properties.label;
              containerCfg.items = [myCatalogLink];
              return ComponentManager.create(containerCfg);
            });

    CMChannelExtension.register(LivecontextStudioPlugin.CONTENT_TYPE_EXTERNAL_PAGE);

    /**
     * Register tab tooltip handler
     */
    EditorContextImpl(editorContext).registerTabTooltipHandler(LivecontextStudioPlugin.CONTENT_TYPE_EXTERNAL_CHANNEL, computeCatalogToolTip);
    EditorContextImpl(editorContext).registerTabTooltipHandler(LivecontextStudioPlugin.CONTENT_TYPE_EXTERNAL_PRODUCT, computeCatalogToolTip);
    EditorContextImpl(editorContext).registerTabTooltipHandler(LivecontextStudioPlugin.CONTENT_TYPE_PRODUCT_TEASER, computeCatalogToolTip);

    EditorContextImpl(editorContext).registerTooltipSkipFlagHandler(tooltipSkipFlagHandler);

    // Colorful Studio styles
    MetaStyleService.setDefaultStyleForMetaData(MetaStyleService.STYLE_GROUP_YELLOW, [LivecontextStudioPlugin.CONTENT_TYPE_PRODUCT_LIST]);
    MetaStyleService.setDefaultStyleForMetaData(MetaStyleService.STYLE_GROUP_PURPLE, [
      LivecontextStudioPlugin.CONTENT_TYPE_EXTERNAL_PAGE, LivecontextStudioPlugin.CONTENT_TYPE_EXTERNAL_CHANNEL]);
    MetaStyleService.setDefaultStyleForMetaData(MetaStyleService.STYLE_GROUP_TURQUOISE, [
      LivecontextStudioPlugin.CONTENT_TYPE_PRODUCT_TEASER, LivecontextStudioPlugin.CONTENT_TYPE_MARKETING_SPOT,
      LivecontextStudioPlugin.CONTENT_TYPE_EXTERNAL_PRODUCT]);
  }

  private static function computeCatalogToolTip(content:Content):Array {
    //this expression points to the catalog object
    var catalogObjectExpression:ValueExpression = CatalogHelper.getInstance().getCatalogExpression(ValueExpressionFactory.createFromValue(content));
    var catalog:Catalog = catalogObjectExpression.extendBy(CatalogObjectPropertyNames.CATALOG).getValue();
    //null means there is no such a thing like catalog
    if (catalog === null) {
      return [];
    }
    if (catalog && catalog.getName()) {
      return [new TabTooltipEntry(CatalogObjectPropertyNames.CATALOG,
              resourceManager.getString('com.coremedia.livecontext.studio.LivecontextStudioPlugin', 'Commerce_catalog_label'), catalog.getName())]
    }
    return undefined;
  }

  /**
   * compute flags for the catalog tooltip entities
   * and collect sites from non-content entities so that
   * they are included in the site-dependent flag computation in EditorContextImpl#computeTooltipSkipFlags
   * @param tooltipSkipFlags
   * @param sites
   * @param entities
   */
  private function tooltipSkipFlagHandler(tooltipSkipFlags:Bean, sites:Array, entities:Array):void {
    var skipCatalog:Boolean = true;
    entities.forEach(function (entity:Object):void {
      var catalog:Catalog;
      if (entity is Content) {
        var content:Content = Content(entity);
        //is the content type registered for the tooltip?
        var tooltipEntries:Array = EditorContextImpl(editorContext).computeAdditionalTabTooltipEntries(content);
        if (tooltipEntries && tooltipEntries.length > 0) {
          var catalogObjectExpression:ValueExpression = CatalogHelper.getInstance().getCatalogExpression(ValueExpressionFactory.createFromValue(content));
          catalog = catalogObjectExpression.extendBy(CatalogObjectPropertyNames.CATALOG).getValue();
        }
      } else if (entity is Product || entity is Category) {
        var site:Site = editorContext.getSitesService().getSite(entity.getSiteId());
        site && sites.push(site);
        catalog = entity.get(CatalogObjectPropertyNames.CATALOG);
      }
      if (catalog && !catalog.isDefault()) {
        skipCatalog = false;
      }
    });


    tooltipSkipFlags.set(CatalogObjectPropertyNames.CATALOG, skipCatalog);

  }

  private static function initProductTeaser(content:Content):void {
    //don't initialize the teaser title for product teasers
    //they will inherit the teaser title form the linked product
    //setProperty(content, 'teaserTitle', content.getName());
    ContentInitializer.initCMLinkable(content);
    ContentInitializer.initCMLocalized(content);
  }

  private static function initMarketingSpot(content:Content):void {
    ContentInitializer.initCMLinkable(content);
    ContentInitializer.initCMLocalized(content);
  }

  private static function initProductList(content:Content):void {
    var localSettings:Struct = content.getProperties().get(LOCAL_SETTINGS_STRUCT_NAME);
    if (!localSettings.get(PRODUCT_LIST_STRUCT_NAME)) {
      localSettings.getType().addStructProperty(PRODUCT_LIST_STRUCT_NAME);
    }
    var productListStruct:Struct = localSettings.get(PRODUCT_LIST_STRUCT_NAME);
    if (!productListStruct.get(OFFSET_NAME)) {
      productListStruct.getType().addIntegerProperty(OFFSET_NAME, 1);
    }
    if (!productListStruct.get(MAX_LENGTH_NAME)) {
      productListStruct.getType().addIntegerProperty(MAX_LENGTH_NAME, 10);
    }
    ContentInitializer.initCMLinkable(content);
    ContentInitializer.initCMLocalized(content);
  }

  private static function initExternalPage(content:Content):void {
    ContentInitializer.initCMLinkable(content);
    ContentInitializer.initCMLocalized(content);
  }

  private static function initExternalChannel(content:Content):void {
    ContentInitializer.initCMLinkable(content);
    ContentInitializer.initCMLocalized(content);
  }

  private static function initExternalProduct(content:Content):void {
    ContentInitializer.initCMLinkable(content);
    ContentInitializer.initCMLocalized(content);
  }

  private static function extendImageMap(content:Content):void {
    var localSettings:Struct = content.getProperties().get('localSettings');
    var overlay:* = localSettings.get("overlay");
    if (overlay) {
      overlay.set("displayDefaultPrice", true);
    }
  }

  //noinspection JSUnusedGlobalSymbols
  internal function getShopExpression():ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():String {
      var store:Store = Store(CatalogHelper.getInstance().getActiveStoreExpression().getValue());
      return store && store.getName();
    });
  }

  static internal function reloadPreview(previewPanel:PreviewPanel):void {
    if (previewPanel.rendered) {
      previewPanel.reloadFrame();
    }
  }

  public static function showInCatalogTree(entity:CatalogObject):void {
    new ShowInCatalogTreeHelper([entity]).showInCatalogTree();
  }

  /////////
  // Utility functions extracted from CatalogHelper
  /////////
  /**
   * Checks if the given category (either content or catalog object) is a part of a site
   * configured for the content led scenario (property 'livecontext.contentLed' in the
   * LiveContext settings document for the Site).
   * @param bindTo
   * @return
   */
  public static function isContentLedValueExpression(bindTo:ValueExpression):ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():Boolean {
      var catalogObjectValueExpression:ValueExpression = AugmentationUtil.toCatalogObjectExpression(bindTo);
      var catalogObject:CatalogObject = catalogObjectValueExpression.getValue() as CatalogObject;

      if (!catalogObject || !catalogObject.getStore()) {
        return undefined;
      }
      var siteId:String = catalogObject.getStore().getSiteId();
      if (!siteId) {
        return undefined;
      }
      var site:Site = editorContext.getSitesService().getSite(siteId);

      if (!site || site === undefined) {
        return undefined;
      }
      if (site.getName() === undefined || site.getLocale() === undefined || site.getLocale().getDisplayName() === undefined) {
        return undefined;
      }
      var rootFolder:Content = site.getSiteRootFolder();
      var liveContextSettings:Content = rootFolder.getChild("Options/Settings/LiveContext");
      if (liveContextSettings === undefined) {
        return undefined;
      }
      if (liveContextSettings !== null) {
        var liveContextSettingsProperties:ContentProperties = liveContextSettings.getProperties();
        if (liveContextSettingsProperties === undefined) {
          return undefined;
        }
        var liveContextStruct:Struct = liveContextSettingsProperties.get("settings") as Struct;
        if (liveContextStruct) {
          var structType:StructType = liveContextStruct.getType();
          if (structType === undefined) {
            return undefined;
          }
          if (structType.hasProperty(CONTENT_LED_PROPERTY)) {
            return liveContextStruct.get(CONTENT_LED_PROPERTY);
          }
          if (structType.hasProperty(MANAGE_NAVIGATION_PROPERTY)) {
            return liveContextStruct.get(MANAGE_NAVIGATION_PROPERTY);
          }
        }
      }
      return false;
    });
  }

  private static function mayCreate(selection:Content, vendorName:String, isBelongsTo:Boolean):Boolean {
    var site:Site = editorContext.getSitesService().getSiteFor(selection);
    if (!site) {
      return false;
    }
    var store:Store = StoreUtil.getStoreForSite(site);
    return isBelongsTo ? CatalogHelper.getInstance().isVendor(store, vendorName) :
            CatalogHelper.getInstance().isNotVendor(store, vendorName);
  }

  internal static function mayCreateProductList(selection:Content):Boolean {
    return mayCreate(selection, VENDOR_CM, false);
  }

  internal static function mayCreateProductTeaser(selection:Content):Boolean {
    return mayCreate(selection, VENDOR_IBM, true) ||
            mayCreate(selection, VENDOR_HYBRIS, true) ||
            mayCreate(selection, VENDOR_SFCC, true);
  }

  internal static function mayCreateESpot(selection:Content):Boolean {
    return mayCreate(selection, VENDOR_IBM, true);
  }
}
}
