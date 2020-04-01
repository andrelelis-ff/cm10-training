package com.coremedia.livecontext.ibm.studio.mgmtcenter {
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ecommerce.studio.model.Category;
import com.coremedia.ecommerce.studio.model.MarketingSpot;
import com.coremedia.ecommerce.studio.model.Product;
import com.coremedia.ecommerce.studio.model.Store;
import com.coremedia.ecommerce.studio.model.StoreImpl;
import com.coremedia.ui.util.UrlUtil;

import ext.ComponentManager;
import ext.Ext;

import js.Window;

public class ManagementCenterUtil {
  public static const DEFAULT_WIDTH:Number = 1000;
  public static const DEFAULT_HEIGHT:Number = 600;

  private static const EXTERNAL_MODE:String = "externalWcs";

  private static var managementWindow:ManagementCenterWindow;
  private static var managementFrame:ManagementCenterFrame;
  private static var myWindow:Window;
  private static var url:String;

  private static function fetchUrl():void {
    var store:StoreImpl = (CatalogHelper.getInstance().getActiveStoreExpression().getValue() as StoreImpl);
    if (!store) {
      url = undefined;
      return;
    }

    if (!store.isLoaded()) {
      store.load(fetchUrl);
    }

    url = store.getVendorUrl();
  }

  public static function getUrl():String {
    fetchUrl();
    return url;
  }

  private static function isExternal():Boolean {
    return isSupportedBrowser() && UrlUtil.getHashParam(EXTERNAL_MODE);
  }

  private static function isNewWindow():Boolean {
    if (isExternal()) {
      return !myWindow;
    } else {
      return !managementWindow
    }
  }

  /**
   * Displays a given product in the Management Center by sending the required product information
   * to the management window.
   * @param product The product to display.
   */
  public static function openProduct(product:Product = undefined):void {
    if (product) {
      openManagementCenterViewInternal();
      var partNumber:String = product.getExternalId();
      var productId:String = product.getExternalTechId();
      var store:Store = product.getStore();
      var msg:String = "product:"+store.getStoreId()+":0:"+partNumber+":"+productId;
      openManagementCenterViewInternal(msg);
    }
  }

  public static function openCategory(category:Category = undefined):void {
    if (category) {
      openManagementCenterViewInternal();
      var partNumber:String = category.getExternalId();
      var categoryId:String = category.getExternalTechId();
      var store:Store = category.getStore();
      var msg:String = "category:"+store.getStoreId()+":0:"+partNumber+":"+categoryId;
      openManagementCenterViewInternal(msg);
    }
  }

  public static function openMarketingSpot(espot:MarketingSpot = undefined):void {
      if (espot) {
        openManagementCenterViewInternal();
        var partNumber:String = espot.getExternalId();
        var store:Store = espot.getStore();
        var msg:String = "espot:"+store.getStoreId()+":0:"+partNumber+":";
        openManagementCenterViewInternal(msg);
      }
  }

  public static function openManagementCenterView():void {
    openManagementCenterViewInternal();
  }

  /**
   * Open the (singleton) Management Center from the {@see ext.WindowManager}.
   */
  private static function openManagementCenterViewInternal(msg:String = undefined): void {
    var wasNewWindow:Boolean = isNewWindow();
    if (isExternal()) {
      openExternal();
    } else {
      openEmbedded();
    }

    if (wasNewWindow && !isExternal()) {
      managementFrame.on('afterrender', function():void {
        myWindow.focus();
      });
      window.setTimeout(function():void {
        openManagementCenterViewInternal(msg);
      }, 7000); // wait 7s when frame was opened initially
      return;
    } else {
      myWindow.focus();
    }

    if (isExternal() && !Ext.isChrome) {
      myWindow.location.replace(getUrl() + "#"+msg);
    }
    else {
      //we have to access the contentWindow anew otherwise the management center integration will not work.
      if (!isExternal()) {
        myWindow = managementFrame.getContentWindow();
      }
      myWindow.postMessage(msg,"*"); // todo: set a real postOrigin instead of "*"
    }
  }

  private static function openExternal():void {
    if (!myWindow || myWindow.closed) {
      myWindow = window.open(getUrl(), "_blank", getWindowOptions());
    }
  }

  private static function openEmbedded():void {
    managementWindow = ComponentManager.get(ManagementCenterWindow.MANAGEMENT_CENTER_WINDOW_ID) as ManagementCenterWindow;
    if (!managementWindow) {
      var windowConfig:ManagementCenterWindow = ManagementCenterWindow({});
      windowConfig.minWidth = DEFAULT_WIDTH;
      windowConfig.width = DEFAULT_WIDTH;
      windowConfig.minHeight = DEFAULT_HEIGHT;
      windowConfig.height = DEFAULT_HEIGHT;
      managementWindow = new ManagementCenterWindow(windowConfig);
      managementFrame = managementWindow.queryById(ManagementCenterWindow.MANAGEMENT_CENTER_FRAME_ID) as ManagementCenterFrame;
      managementFrame.on('afterrender', function():void {
        myWindow = managementFrame.getContentWindow();
      });
    }
    managementWindow.show();
  }

  private static function getWindowOptions():String {
    var widthOption:String = "width=" + DEFAULT_WIDTH;
    var heightOption:String = "height=" + DEFAULT_HEIGHT;
    return widthOption + "," + heightOption + ",location=yes, resizable=yes";
  }


  public static function isSupportedBrowser(catalogObjects:Array = null):Boolean {
    var store:Store;
    if (catalogObjects && catalogObjects.length > 0) {
      store = CatalogObject(catalogObjects[0]).getStore();
    } else {
      store = CatalogHelper.getInstance().getActiveStoreExpression().getValue();
    }
    if (store) {
      if (store.getVendorName() !== "IBM") {
        return false;
      }
      var wcsVersion:Vector.<int> = parseWcsVersion(store.getVendorVersion() as String);
      if (wcsVersion[0] > 7 || wcsVersion[0] == 7 && wcsVersion[1] > 7) {
        return Ext.isIE || Ext.isGecko || Ext.isChrome;
      }
    }
    return Ext.isIE || Ext.isGecko;
  }

  private static function parseWcsVersion(version:String):Vector.<int> {
    return Vector.<int>((version || "").split(".").map(toInt));
  }

  private static function toInt(s:String):int {
    return parseInt(s, 10);
  }
}
}