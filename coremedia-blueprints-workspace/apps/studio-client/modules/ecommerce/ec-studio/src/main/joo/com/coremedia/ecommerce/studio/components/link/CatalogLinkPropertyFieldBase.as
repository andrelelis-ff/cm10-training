package com.coremedia.ecommerce.studio.components.link {
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.premular.fields.LinkListGridPanel;
import com.coremedia.cms.editor.sdk.util.ILinkListWrapper;
import com.coremedia.cms.editor.sdk.util.ImageLinkListRenderer;
import com.coremedia.cms.editor.sdk.util.PropertyEditorUtil;
import com.coremedia.ecommerce.studio.helper.AugmentationUtil;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ecommerce.studio.model.CatalogObjectPropertyNames;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.util.ThumbnailImage;

public class CatalogLinkPropertyFieldBase extends LinkListGridPanel {

  private var _localWrapper:ILinkListWrapper;

  [Bindable]
  public var bindTo:ValueExpression;

  [Bindable]
  public var model:Bean;

  [Bindable]
  public var propertyName:String;

  [Bindable]
  public var maxCardinality:Number;

  [Bindable]
  public var createStructFunction:Function;

  [Bindable]
  public var linkTypeNames:Array;

  [Bindable]
  public var forceReadOnlyValueExpression:ValueExpression;

  public function CatalogLinkPropertyFieldBase(config:CatalogLinkPropertyField = null) {
    super(config);
    bindTo = config.bindTo;
  }

  protected function getLinkListWrapper(config:CatalogLinkPropertyFieldBase):ILinkListWrapper {
    if (!_localWrapper) {
      if (config.linkListWrapper) {
        _localWrapper = config.linkListWrapper;
      } else {
        var wrapperCfg:CatalogLinkListWrapper = CatalogLinkListWrapper({});
        wrapperCfg.bindTo = config.bindTo;
        wrapperCfg.model = config.model;
        wrapperCfg.propertyName = config.propertyName;
        wrapperCfg.maxCardinality = config.maxCardinality;
        wrapperCfg.createStructFunction = config.createStructFunction;
        wrapperCfg.linkTypeNames = config.linkTypeNames;
        wrapperCfg.readOnlyVE = getReadOnlyVE(config);
        _localWrapper = new CatalogLinkListWrapper(wrapperCfg);
      }
    }
    return _localWrapper;
  }

  protected function getReadOnlyVE(config:CatalogLinkPropertyFieldBase):ValueExpression {
    if (!readOnlyValueExpression) {
      if (config.readOnlyValueExpression) {
        readOnlyValueExpression = config.readOnlyValueExpression;
      } else {
        readOnlyValueExpression = PropertyEditorUtil.createReadOnlyValueExpression(config.bindTo, config.forceReadOnlyValueExpression);
      }

    }
    return readOnlyValueExpression;
  }

  internal static function convertTypeLabel(v:String, catalogObject:CatalogObject):String {
    if (catalogObject is CatalogObject) {
      return AugmentationUtil.getTypeLabel(catalogObject);
    }
  }

  internal static function convertTypeCls(v:String, catalogObject:CatalogObject):String {
    if (catalogObject is CatalogObject) {
      return AugmentationUtil.getTypeCls(catalogObject);
    }
  }

  internal static function convertIdLabel(v:String, catalogObject:CatalogObject):String {
    if (!catalogObject) {
      return undefined;
    }
    var extId:String = catalogObject.getExternalId();
    if (extId) {
      return extId;
    } else if (extId === null) {
      return CatalogHelper.getInstance().getExternalIdFromId(catalogObject.getUri());
    }
    return undefined;
  }

  internal static function convertNameLabel(v:String, catalogObject:CatalogObject):String {
    var name:String = undefined;
    if (!catalogObject) {
      return name;
    }
    if (catalogObject is CatalogObject) {
      try {
        name = CatalogHelper.getInstance().getDecoratedName(catalogObject);
      } catch (e:Error) {
        //ignore
      }
    }
    if (!name) {
      name = CatalogHelper.getInstance().getExternalIdFromId(catalogObject.getUri());
    }
    return name;
  }

  internal static function convertLifecycleStatus(v:String, catalogObject:CatalogObject):String {
    if (catalogObject is CatalogObject) {
      var augmentingContent:Content = catalogObject.get(CatalogObjectPropertyNames.CONTENT) as Content;
      if (augmentingContent) { // the commerce object has been augmented
        return augmentingContent.getLifecycleStatus();
      }
    }
    return undefined;
  }

  public static function convertThumbnail(model:Object):ThumbnailImage {
    return ImageLinkListRenderer.convertThumbnailFor(model, "CatalogObject");
  }
}
}
