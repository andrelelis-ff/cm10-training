package com.coremedia.blueprint.studio.esanalytics {
import com.coremedia.cap.content.Content;
import com.coremedia.cap.undoc.content.ContentUtil;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.ui.components.StatefulContainer;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.store.BeanRecord;

import ext.form.field.ComboBox;

public class EsAnalyticsChartWidgetEditorBase extends StatefulContainer {

  private var rootChannelValueExpr:ValueExpression;

  public function EsAnalyticsChartWidgetEditorBase(config:EsAnalyticsChartWidgetEditor = null) {
    super(config);

    getRootChannelValueExpression().addChangeListener(rootChannelChanged);
  }

  override protected function onDestroy():void {
    getRootChannelValueExpression().removeChangeListener(rootChannelChanged);
  }

  protected function getSelectedSiteExpression():ValueExpression {
    return ValueExpressionFactory.create("content", getModel());
  }

  protected function getRootChannelValueExpression():ValueExpression {
    if (!rootChannelValueExpr) {
      rootChannelValueExpr = ValueExpressionFactory.createFromFunction(function():Array {
        var siteRootDocs:Array = [];
        var sites:Array = editorContext.getSitesService().getSites();
        if (sites) {
          sites.forEach(function(site:Site):Content {
            siteRootDocs.push(site.getSiteRootDocument());
          });
        }
        return siteRootDocs;
      });
    }
    return rootChannelValueExpr;
  }

  protected static function getContentFromId(id:String):Content {
    return ContentUtil.getContent(id);
  }

  protected static function getIdFromContent(content:Content):String {
    return content ? content.getId() : undefined;
  }

  private function rootChannelChanged():void {
    var comboBox:ComboBox = down("combo") as ComboBox;
    if (comboBox) {
      mon(comboBox.getStore(), "load", function ():void {
        storeLoaded(comboBox);
      });
    }
  }

  private function storeLoaded(comboBox:ComboBox):void {
    var value:* = getSelectedSiteExpression().getValue();
    var index:int = comboBox.getStore().find("id", value);
    if (index >= 0) {
      var beanRecord:BeanRecord = comboBox.getStore().getAt(index) as BeanRecord;
      if (beanRecord.data && beanRecord.data.value) {
        comboBox.setValue(value);
      } else {
        mon(comboBox.getStore(), "update", function ():void {
          setComboBoxValue(comboBox, value);
        });
      }
    }
  }

  private static function setComboBoxValue(comboBox:ComboBox, value:int):void {
    comboBox.setValue(value);
  }
}
}