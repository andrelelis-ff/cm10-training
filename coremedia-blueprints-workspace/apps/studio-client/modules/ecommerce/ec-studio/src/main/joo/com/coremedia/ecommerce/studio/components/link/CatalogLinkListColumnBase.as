package com.coremedia.ecommerce.studio.components.link {
import com.coremedia.cms.editor.sdk.columns.grid.IconColumn;
import com.coremedia.cms.editor.sdk.columns.grid.IconColumnBase;
import com.coremedia.ui.bem.IconWithTextBEMEntities;
import com.coremedia.ui.models.bem.BEMModifier;

import ext.XTemplate;
import ext.data.Model;
import ext.data.Store;
import ext.view.DataView;

public class CatalogLinkListColumnBase extends IconColumnBase {

  /**
   * The icon css class to use for the catalog icon.
   */
  [Bindable]
  public var catalogIconCls:String;

  /**
   * An additional text describing the catalog icon.
   */
  [Bindable]
  public var catalogIconText:String;

  /**
   * A tooltip to display when hoving the catalog block of the column.
   */
  [Bindable]
  public var catalogToolTipText:String;

  /**
   * If true the catalog info will be hidden. Default is false.
   */
  [Bindable]
  public var hideCatalog:Boolean;

  public function CatalogLinkListColumnBase(config:IconColumn = null) {
    super(config);
  }

  /**
   * The dataIndex of the catalog object id.
   *
   * @see ext.grid.column.Column.dataIndex
   */
  [Bindable]
  public var catalogObjectIdDataIndex:String;

  /**
   * The dataIndex of the catalog object name.
   *
   * @see ext.grid.column.Column.dataIndex
   */
  [Bindable]
  public var catalogObjectNameDataIndex:String;

  /**
   * The dataIndex of the catalog name.
   *
   * @see ext.grid.column.Column.dataIndex
   */
  [Bindable]
  public var catalogNameDataIndex:String;

  [Bindable]
  public var multiCatalogDataIndex:String;

  private static const MODIFIER_SECOND_ITEM:BEMModifier = IconWithTextBEMEntities.BLOCK.createModifier("second-item");

  override protected function getRenderer(value:*, metadata:*, record:Model, rowIndex:Number, colIndex:Number, store:Store, view:DataView):String {
    return this.tpl.apply({
      modifiers: getModifierCls(calculateModifier(value, metadata, record, rowIndex, colIndex, store)),
      iconCls: calculateIconCls(value, metadata, record, rowIndex, colIndex, store) || "",
      iconText: calculateIconText(value, metadata, record, rowIndex, colIndex, store) || "",
      toolTipText: calculateToolTipText(value, metadata, record, rowIndex, colIndex, store) || "",
      catalogIconCls: catalogIconCls || "",
      catalogIconText: catalogIconText || "",
      catalogToolTipText: catalogToolTipText || "",
      catalogObjectId: record.get(catalogObjectIdDataIndex),
      catalogObjectName: record.get(catalogObjectNameDataIndex),
      catalog: !hideCatalog && record.get(catalogNameDataIndex),
      multiCatalog: record.get(multiCatalogDataIndex)
    });
  }

  protected static function getXTemplate():XTemplate {
    var xTemplate:XTemplate = new XTemplate([
      '<div aria-label="{iconText:escape}" class="' + IconWithTextBEMEntities.BLOCK + ' {modifiers:escape}" {toolTipText:unsafeQtip}>',
      '  <span class="' + IconWithTextBEMEntities.ELEMENT_ICON + ' {iconCls:escape}"></span>',
      '  <span style="width: 0px;position:absolute;overflow:hidden;">{iconText:escape}</span>',
      '  <span class="' + IconWithTextBEMEntities.ELEMENT_TEXT + '">{catalogObjectId} <tpl if="catalogObjectId !== catalogObjectName">({catalogObjectName})</tpl></span>',
      '</div>',
      '<tpl if="catalog && multiCatalog"><div aria-label="{catalogIconText:escape}" class="' + IconWithTextBEMEntities.BLOCK + ' ' + MODIFIER_SECOND_ITEM + '" {catalogToolTipText:unsafeQtip}>',
      '  <span class="' + IconWithTextBEMEntities.ELEMENT_ICON + ' {catalogIconCls:escape}"></span>',
      '  <span style="width: 0px;position:absolute;overflow:hidden;">{catalogIconText:escape}</span>',
      '  <span class="' + IconWithTextBEMEntities.ELEMENT_TEXT + '">{catalog}</span>',
      '</div></tpl>'
    ]);
    return xTemplate;
  }
}
}