package com.coremedia.blueprint.studio.forms.media {
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.models.bem.BEMBlock;
import com.coremedia.ui.models.bem.BEMElement;

import ext.XTemplate;

import ext.container.Container;

public class MetaDataViewBase extends Container {

  public static const PROPERTIES_BLOCK:BEMBlock = new BEMBlock("meta-data-view");
  public static const PROPERTIES_ELEMENT_LABEL:BEMElement = PROPERTIES_BLOCK.createElement("label");
  public static const PROPERTIES_ELEMENT_TEXT:BEMElement = PROPERTIES_BLOCK.createElement("text");

  private var metaDataExpression:ValueExpression;

  public function MetaDataViewBase(config:MetaDataView = null) {
    super(config);
  }

  public function getMetaDataExpression(metaDataSection:MetaDataSection):ValueExpression {
    if (!metaDataExpression) {
      metaDataExpression = ValueExpressionFactory.createFromValue(metaDataSection.getData());
    }
    return metaDataExpression;
  }

  protected static function getXTemplate():XTemplate {
    var xTemplate:XTemplate = new XTemplate([
      '<tpl for=".">',
        '<div class="' + PROPERTIES_BLOCK + '">',
          '<div class="' + PROPERTIES_ELEMENT_LABEL + '">',
            '{property}:',
          '</div>',
          '<div data-qtip="{value:htmlEncode}" class="' + PROPERTIES_ELEMENT_TEXT + '">',
            '{formattedValue}',
          '</div>',
        '</div>',
      '</tpl>'
    ]);
    return xTemplate;
  }
}
}