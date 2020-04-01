package com.coremedia.blueprint.studio {
import ext.Action;
import ext.Ext;

public class OpenOptimizelyServiceUrlActionBase extends Action {

  private static var url:String = "https://www.optimizely.com/";

  /**
   * @cfg {String} url to the home page.
   *
   * @param config the config object
   */
  public function OpenOptimizelyServiceUrlActionBase(config:OpenOptimizelyServiceUrlAction = null) {
    config['handler'] = openInBrowser;
    if (config['url']) {
      url = config['url'];
    }
    super(Action(Ext.apply({}, config, {handler:openInBrowser})));
    this.setDisabled(false);
  }

  /**
   * The action opens the url in a new browser.
   */
  private function openInBrowser():void {
    var urlToOpen:String = url;
    var wname:String = "Optimizely";
    var wfeatures:String = "menubar=yes,resizable=yes,scrollbars=yes,status=yes,location=yes";
    window.open(urlToOpen, wname, wfeatures);
  }

}
}