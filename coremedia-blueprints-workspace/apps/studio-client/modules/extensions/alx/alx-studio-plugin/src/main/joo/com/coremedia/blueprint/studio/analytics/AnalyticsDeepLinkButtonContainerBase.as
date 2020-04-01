package com.coremedia.blueprint.studio.analytics {

import com.coremedia.ui.components.IconButton;
import com.coremedia.ui.skins.ButtonSkin;

import ext.Ext;
import ext.button.Button;
import ext.container.Container;
import ext.menu.Item;
import ext.menu.Menu;

[ResourceBundle('com.coremedia.icons.CoreIcons')]
[ResourceBundle('com.coremedia.blueprint.studio.analytics.AnalyticsStudioPlugin')]
internal class AnalyticsDeepLinkButtonContainerBase extends Container {
  public function AnalyticsDeepLinkButtonContainerBase(config:Container = null) {
    super(config);
    addListener('beforerender', onBeforeRender);
  }

  private function onBeforeRender():void {
    if (itemCollection && itemCollection.length > 1) {
      renderButtonMenu();
    }
  }

  private function renderButtonMenu():void {
    const menuItemsFromButtons:Array = [];

    // iterate through all buttons and create new menuItems out of them
    itemCollection.each(function (item:OpenAnalyticsDeepLinkUrlButton):void {
      var config:Object = item.initialConfig;
      delete config.iconCls;
      var menuItem:Item = createMenuItem(item);
      menuItem.addListener('enable', updateAnalyticsReportButton);
      menuItem.addListener('disable', updateAnalyticsReportButton);

      menuItemsFromButtons.push(menuItem);
    });

    // add menuItems to button menu
    var menuCfg:Menu = Menu({});
    menuCfg.items = menuItemsFromButtons;
    menuCfg['allowFunctions'] = true;
    menuCfg.width = 233;
    var buttonMenu:Menu = new Menu(menuCfg);

    // create menu button that holds the above menu
    var buttonCfg:IconButton = IconButton({});
    buttonCfg.iconCls = resourceManager.getString('com.coremedia.icons.CoreIcons', 'analytics');
    buttonCfg.ui = ButtonSkin.WORKAREA.getSkin();
    buttonCfg.scale = 'medium';
    buttonCfg.itemId = 'analyticsReportButton';
    buttonCfg.text = resourceManager.getString('com.coremedia.blueprint.studio.analytics.AnalyticsStudioPlugin', 'multi_analytics_button_text');
    buttonCfg.tooltip = resourceManager.getString('com.coremedia.blueprint.studio.analytics.AnalyticsStudioPlugin', 'multi_analytics_button_tooltip');
    buttonCfg.disabled = true;
    buttonCfg.menu = buttonMenu;
    var newButton:IconButton = new IconButton(buttonCfg);

    add(newButton);
  }

  internal function updateAnalyticsReportButton():void {
    var component:Button = this.getComponent('analyticsReportButton') as Button;
    var allMenuItemsDisabled:Boolean = component.menu.itemCollection.getRange().every(function (item:Item):Boolean {
      return item.disabled;
    });
    component.setDisabled(allMenuItemsDisabled);
  }

  internal function createMenuItem(item:OpenAnalyticsDeepLinkUrlButton):Item {
    // make sure button is properly initialized
    if (!item.urlValueExpression) {
      item.initUrlValueExpression();
    }

    // copy config - except 'xtype'!
    var buttonConfig:OpenAnalyticsDeepLinkUrlButton = OpenAnalyticsDeepLinkUrlButton(item.initialConfig);
    var menuConfig:Object = {
      iconCls: resourceManager.getString('com.coremedia.icons.CoreIcons', 'analytics'),
      handler: OpenAnalyticsUrlButtonBase.openInBrowser(item.urlValueExpression, buttonConfig.windowName),
      text: buttonConfig.tooltip,
      urlValueExpression: item.urlValueExpression,
      contentExpression: item.contentExpression
    };
    Ext.apply(menuConfig, buttonConfig);
    delete menuConfig.xtype;
    delete menuConfig.xclass;

    var result:Item = new Item(Item(menuConfig));
    OpenAnalyticsUrlButtonBase.bindDisable(item.urlValueExpression, result);

    //hide single button
    item.setVisible(false);

    return result;
  }
}
}
