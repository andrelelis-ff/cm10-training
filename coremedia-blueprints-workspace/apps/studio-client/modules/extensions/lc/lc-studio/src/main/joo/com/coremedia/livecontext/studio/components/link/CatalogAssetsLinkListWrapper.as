package com.coremedia.livecontext.studio.components.link {
import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cms.editor.sdk.util.IContentAwareLinkListWrapper;
import com.coremedia.cms.editor.sdk.util.LinkListUtil;
import com.coremedia.cms.editor.sdk.util.LinkListWrapperBase;
import com.coremedia.cms.editor.sdk.util.PropertyEditorUtil;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

public class CatalogAssetsLinkListWrapper extends LinkListWrapperBase implements IContentAwareLinkListWrapper {

  [Bindable]
  public var bindTo:ValueExpression;

  [Bindable]
  public var linksVE:ValueExpression;

  [Bindable]
  public var assetContentTypes:Array;

  [Bindable]
  public var maxCardinality:int;

  [Bindable]
  public var readOnlyVE:ValueExpression;

  public function CatalogAssetsLinkListWrapper(config:CatalogAssetsLinkListWrapper = null) {
    super(config);
    bindTo = config.bindTo;
    linksVE = config.linksVE;
    assetContentTypes = config.assetContentTypes || [];
    maxCardinality = config.maxCardinality || 0;
    readOnlyVE = config.readOnlyVE;
  }

  override public function getVE():ValueExpression {
    return linksVE;
  }

  override public function acceptsLinks(links:Array):Boolean {
    for each (var asset:Content in links) {
      if (!(asset is Content)) {
        return false;
      }

      //check the content type
      var typeAccepted:Boolean = assetContentTypes.some(function(assetContentType:String):Boolean {
        return LinkListUtil.containsContentNotMatchingType(getContentType(assetContentType), [asset]) === null;
      });
      if (!typeAccepted) {
        return false;
      }

      if (PropertyEditorUtil.isReadOnly(asset)) {
        return false;
      }
    }
    return true;
  }

  override public function getLinks():Array {
    var value:* = getVE().getValue();
    return value === undefined ? undefined : value as Array;
  }

  override public function setLinks(links:Array):void {
    if (links) {
      for each (var content:Content in links) {
        if (!PropertyEditorUtil.isReadOnly(content)) {
          CatalogHelper.getInstance().createOrUpdateProductListStructs(ValueExpressionFactory.createFromValue(content),
                  bindTo.getValue());
        }
      }
    }
  }

  override public function getTotalCapacity():int {
    return maxCardinality > 0 ? maxCardinality : int.MAX_VALUE;
  }

  override public function getFreeCapacity():int {
    return getTotalCapacity() - (getLinks() ? getLinks().length : 0);
  }

  override public function isReadOnly():Boolean {
    return readOnlyVE ? readOnlyVE.getValue() : false;
  }

  private static function getContentType(linkTypeName:String):ContentType {
    return SESSION.getConnection().getContentRepository().getContentType(linkTypeName) as ContentType;
  }

  public function isAccepted(contentType:ContentType):Boolean {
    var allowedContentTypes:Array = getAllowedContentType(assetContentTypes);
    return allowedContentTypes.some(function (allowedContentType: ContentType): Boolean {
      return contentType.isSubtypeOf(allowedContentType);
    });
  }

  public function getOwnerContent():Content {
    return bindTo.getValue() as Content;
  }

  private static function getAllowedContentType(contentTypeNames: Array): Array {
    var allowedContentTypes: Array = [];
    for (var allowedContentType:String in contentTypeNames) {
      var contentType:ContentType = getContentType(allowedContentType);
      if(contentType) {
        allowedContentTypes.push(contentType);
      }
    }
    return allowedContentTypes;
  }
}
}
