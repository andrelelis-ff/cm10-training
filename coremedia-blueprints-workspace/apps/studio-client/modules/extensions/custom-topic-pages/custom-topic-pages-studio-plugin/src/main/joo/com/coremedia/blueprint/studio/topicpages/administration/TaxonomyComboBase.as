package com.coremedia.blueprint.studio.topicpages.administration {
import com.coremedia.cap.content.Content;
import com.coremedia.cap.undoc.content.ContentUtil;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ui.components.LocalComboBox;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;

import ext.data.Model;
import ext.data.Store;
import ext.form.field.ComboBox;

/**
 * The base class of the taxonomy combo.
 * The taxonomy combo displays all available taxonomies, global and site depending ones.
 */
public class TaxonomyComboBase extends LocalComboBox {
  /**
   * Contains the selected taxonomy
   */
  [Bindable]
  public var selectionExpression:ValueExpression;

  [Bindable]
  public var filterExpression:ValueExpression;

  private var taxonomiesExpression:ValueExpression;

  public function TaxonomyComboBase(config:TaxonomyComboBase = null) {
    super(config);
  }

  override protected function afterRender():void {
    super.afterRender();
    on('select', valueSelected);
    getStore().on('load', storeLoaded);
  }

  /**
   * The selection listener method for the combo box.
   * @param combo the combo box
   * @param record the selected record
   */
  private function valueSelected(combo:ComboBox, record:Model):void {
    var id:String = record.data.id;
    var content:Content = ContentUtil.getContent(id);
    filterExpression.setValue('');
    selectionExpression.setValue(content);
  }

  /**
   * Selects the first record in the combo box and propagates the selection.
   *
   * @param store the store of the combo box
   * @param records the store records
   * @param successful if loading was successful
   */
  private function storeLoaded(store:Store, records:Array, successful:Boolean):void {
    if (successful && records.length > 0) {
      var r1:Model = records[0];
      // set the value in the combo box
      setValue(r1.data.id);
      // propagate selection
      valueSelected(this, r1);
    }
  }

  /**
   * Returns the value expression that contains the all taxonomies.
   * @return
   */
  protected function getTaxonomiesExpression():ValueExpression {
    if(!taxonomiesExpression) {
      taxonomiesExpression = ValueExpressionFactory.createFromFunction(function():Array {
        var records:Array = [];
        var remoteBean:RemoteBean = beanFactory.getRemoteBean('topicpages/taxonomies');
        if(!remoteBean.isLoaded()) {
          remoteBean.load();
          return undefined;
        }

        var values:Array = remoteBean.get('items');
        for each(var value:Content in values) {
          if(!value.isLoaded()) {
            value.load();
            return undefined;
          }

          if(value.getPath() == undefined) {
            return undefined;
          }

          records.push({id:value.getId(), path:formatDisplayName(value)});
        }

        return records;
      });
    }
    return taxonomiesExpression;
  }

  protected static function formatDisplayName(content:Content):String {
    var site:String = editorContext.getSitesService().getSiteNameFor(content);
    if(site) {
      return content.getName() + ' (' + site + ')';
    }
    return content.getName();
  }
}
}