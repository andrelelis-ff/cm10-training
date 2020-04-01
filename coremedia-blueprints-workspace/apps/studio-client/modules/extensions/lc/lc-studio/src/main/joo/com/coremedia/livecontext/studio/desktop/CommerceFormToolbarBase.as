package com.coremedia.livecontext.studio.desktop {
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.ecommerce.studio.helper.AugmentationUtil;
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ui.components.IconDisplayField;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.toolbar.Toolbar;

public class CommerceFormToolbarBase extends Toolbar {

  private var localeNameValueExpression:ValueExpression;


  /**
   * Create a new instance.
   */
  public function CommerceFormToolbarBase(config:CommerceFormToolbar = null) {
    super(config);
  }

  /**
   * a value expression to the Commerce Object to create this toolbar for
   */
  [Bindable]
  public var bindTo:ValueExpression;

  protected function getCatalogObject():CatalogObject {
    return bindTo.getValue() as CatalogObject;
  }


  internal function getLocaleValueExpression():ValueExpression {
    if (!localeNameValueExpression) {
      localeNameValueExpression = ValueExpressionFactory.createFromFunction(function ():Object {
        var catalogObject:CatalogObject = getCatalogObject();
        if (!catalogObject.getStore()) return undefined;
        if (!catalogObject.getStore().getSiteId()) return undefined;
        var site:Site = editorContext.getSitesService().getSite(catalogObject.getStore().getSiteId());
        if (site === undefined) return undefined;
        if (site && site.getLocale()) {
          var displayName:String = site.getLocale().getDisplayName();
          return {text: displayName, help: displayName, visible: true};
        } else {
          return {text: '', help: '', visible: false};
        }
      });
    }
    return localeNameValueExpression;
  }

  public static function changeLocale(component:IconDisplayField, valueExpression:ValueExpression):void {
    var model:Object = valueExpression.getValue();

    if (model) {
      var text:String = model.text;

      component.setVisible(model.visible);
      component.value = text;
      component.tooltip = model.help;
    }
  }

  public static function changeType(iconDisplayField:IconDisplayField, valueExpression:ValueExpression):void {
    var catalogObject:CatalogObject = valueExpression.getValue();
    if (!catalogObject) {
      return;
    }
    var iconStyleClass:String = AugmentationUtil.getTypeCls(catalogObject);
    var text:String = AugmentationUtil.getTypeLabel(catalogObject);
    iconDisplayField.iconCls = iconStyleClass;
    iconDisplayField.value = text;
  }
}
}
