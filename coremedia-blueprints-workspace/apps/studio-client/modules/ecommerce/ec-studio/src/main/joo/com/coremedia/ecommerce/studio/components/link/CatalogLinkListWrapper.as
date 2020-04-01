package com.coremedia.ecommerce.studio.components.link {
import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.cms.editor.sdk.util.IContentAwareLinkListWrapper;
import com.coremedia.cms.editor.sdk.util.LinkListWrapperBase;
import com.coremedia.ecommerce.studio.augmentation.augmentationService;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.BeanState;
import com.coremedia.ui.data.PropertyChangeEvent;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.logging.Logger;

public class CatalogLinkListWrapper extends LinkListWrapperBase implements IContentAwareLinkListWrapper {

  [Bindable]
  public var bindTo:ValueExpression;

  [Bindable]
  public var propertyName:String;

  [Bindable]
  public var linkTypeNames:Array;

  [Bindable]
  public var maxCardinality:int;

  [Bindable]
  public var model:Bean;

  [Bindable]
  public var createStructFunction:Function;

  [Bindable]
  public var readOnlyVE:ValueExpression;

  private var linksVE:ValueExpression;
  private var propertyExpression:ValueExpression;
  private var catalogObjRemoteBean:RemoteBean;

  public function CatalogLinkListWrapper(config:CatalogLinkListWrapper = null) {
    bindTo = config.bindTo;
    propertyName = config.propertyName;
    linkTypeNames = config.linkTypeNames;
    maxCardinality = config.maxCardinality || 0;
    model = config.model;
    createStructFunction = config.createStructFunction;
    readOnlyVE = config.readOnlyVE;
  }

  override public function getVE():ValueExpression {
    if (!linksVE) {
      linksVE = ValueExpressionFactory.createTransformingValueExpression(getPropertyExpression(),
              transformer, reverseTransformer, []);
    }
    return linksVE;
  }

  private function invalidateIssues(event:PropertyChangeEvent):void {
    if (event.newState === BeanState.NON_EXISTENT || event.oldState === BeanState.NON_EXISTENT) {
      var content:Content = bindTo.getValue() as Content;
      if (content && content.getIssues()) {
        content.getIssues().invalidate();
      }
    }
  }

  protected function getPropertyExpression():ValueExpression {
    if (!propertyExpression) {
      if (bindTo) {
        if (bindTo.getValue() is Content) {
          propertyExpression = bindTo.extendBy('properties').extendBy(propertyName);
        } else {
          propertyExpression = bindTo.extendBy(propertyName);
        }
      } else {
        propertyExpression = ValueExpressionFactory.create(propertyName, model);
      }
    }
    return propertyExpression;
  }

  override public function getTotalCapacity():int {
    return maxCardinality > 0 ? maxCardinality : int.MAX_VALUE;
  }

  override public function getFreeCapacity():int {
    if (!maxCardinality) {
      return int.MAX_VALUE;
    }
    //noinspection JSMismatchedCollectionQueryUpdate
    var catalogItems:Array = getVE().getValue() as Array;
    return maxCardinality - catalogItems.length;
  }

  override public function acceptsLinks(links:Array):Boolean {
    var targetSiteId:String = getTargetSiteId();
    return links.every(function(link:Object):Boolean {
      var catalogObject:CatalogObject = getCatalogObject(link);
      if (!catalogObject) {
        return false;
      }
      if (catalogObject.getSiteId() !== targetSiteId) {
        return false;
      }

      return linkTypeNames.some(function(linkTypeName:String):Boolean {
        return CatalogHelper.getInstance().isSubType(catalogObject, linkTypeName);
      });
    });
  }

  /**
   * Return the site of the currently bound content or the preferred site.
   * May be undefined.
   */
  private function getTargetSite():Site {
    if (bindTo) {
      var content:Content = bindTo.getValue() as Content;
      if (content) {
        return editorContext.getSitesService().getSiteFor(content);
      }
    }
    //no content there. so let's take the preferred site
    return editorContext.getSitesService().getPreferredSite();
  }

  /**
   * Return the the target site id. May be undefined.
   */
  private function getTargetSiteId():String {
    var site:Site = getTargetSite();
    return site && site.getId();
  }

  private static function getCatalogObject(link:Object):CatalogObject {
    var catalogObject:CatalogObject = link as CatalogObject;
    if (!catalogObject) {
      var content:Content = link as Content;
      if (content) {
        catalogObject = augmentationService.getCatalogObject(content);
      }
    }
    return catalogObject;
  }

  override public function getLinks():Array {
    return getVE().getValue();
  }

  override public function setLinks(links:Array):void {
    if (createStructFunction) {
      createStructFunction.apply();
    }
    var myLinks:Array = links.map(getCatalogObject);
    //are some links yet not loaded?
    //noinspection JSMismatchedCollectionQueryUpdate
    var notLoadedLinks:Array = myLinks.filter(function(myLink:CatalogObject):Boolean {
      return !myLink.isLoaded();
    });
    if (!notLoadedLinks || notLoadedLinks.length === 0) {
      getVE().setValue(myLinks);
    } else {
      notLoadedLinks.every(function(notLoadedLink:CatalogObject):void {
        notLoadedLink.load(function():void {
          setLinks(myLinks);
        });
      });
    }
  }

  override public function isReadOnly():Boolean {
    return readOnlyVE ? readOnlyVE.getValue() : false;
  }

  private function transformer(value:*):Array {
    var valuesArray:Array = [];
    if (value) {
      //the value can be a string or a catalog object bean
      if (value is String || value is CatalogObject) {
        //this is a single catalog object stored
        valuesArray = [value];
      } else if (value is Array) {
        //this are multiple catalog objects stored in an array
        valuesArray = value;
      }
    }

    return valuesArray.map(function (value:*):CatalogObject {
      //the value can be a string or a catalog object bean
      var catalogObject:CatalogObject;

      if (value is CatalogObject) {
        catalogObject = value;
      } else if (value is String) {
        catalogObject = CatalogHelper.getInstance().getCatalogObject(value, bindTo) as CatalogObject;
      } else {
        Logger.error("CatalogLink does not accept the value: " + value);
      }

      if (catalogObject === undefined) {
        return undefined;
      }

      if (catalogObject !== catalogObjRemoteBean) {
        if (catalogObjRemoteBean) {
          catalogObjRemoteBean.removePropertyChangeListener(BeanState.PROPERTY_NAME, invalidateIssues);
        }
        catalogObjRemoteBean = catalogObject;
        if (catalogObject) {
          catalogObjRemoteBean.addPropertyChangeListener(BeanState.PROPERTY_NAME, invalidateIssues);
        }
      }
      return catalogObject;
    });
  }

  private function reverseTransformer(value:Array):* {
    if (value && value.length > 0) {
      if (maxCardinality === 1) {
        return CatalogObject(value[0]).getId();
      } else {
        return value.map(function (bean:CatalogObject):String {
          return bean.getId();
        })
      }
    }
    return maxCardinality === 1 ? "" : [];
  }

  public function isAccepted(contentType:ContentType):Boolean {
    var allowedContentTypes:Array = getAllowedContentTypes();
    return allowedContentTypes.some(function (allowedContentType: ContentType): Boolean {
      return contentType.isSubtypeOf(allowedContentType);
    });
  }

  private function getAllowedContentTypes(): Array {
    var allowedContentTypes: Array = [];

    for (var contentTypeName: String in linkTypeNames) {
      var contentType:ContentType = SESSION.getConnection().getContentRepository().getContentType(contentTypeName);
      if (contentType) {
        allowedContentTypes.push(contentType);
      }
    }
    return allowedContentTypes;
  }

  public function getOwnerContent():Content {
    return bindTo.getValue() as Content;
  }
}
}
