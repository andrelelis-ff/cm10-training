package com.coremedia.livecontext.studio {
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.util.ThumbnailResolver;
import com.coremedia.ecommerce.studio.catalogHelper;
import com.coremedia.ecommerce.studio.helper.CatalogHelper;
import com.coremedia.ecommerce.studio.model.CatalogObject;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

public class CatalogThumbnailResolver implements ThumbnailResolver {

  private var docType:String;

  public function CatalogThumbnailResolver(docType:String):void {
    this.docType = docType;
  }


  public function getContentType():String {
    return docType;
  }

  public function getThumbnail(model:Object, operations:String = null):Object {
    if(model as Content) {
      return renderLiveContextPreview(model as Content);
    }

    var url:String = catalogHelper.getImageUrl(model as CatalogObject);
    if(url) {
      return url;
    }
    return null;
  }

  /**
   * Since all live context bean use the "externalId" property we can register the same
   * rendering function for all content types.
   * @param content The livecontext content to render.
   * @return The preview url of the catalog object.
   */
  protected function renderLiveContextPreview(content:Content):Object {
    var blob:String = undefined;
    var contentExpression:ValueExpression = ValueExpressionFactory.createFromValue(content);
    var externalIdExpression:ValueExpression = contentExpression.extendBy('properties.' + LivecontextStudioPlugin.EXTERNAL_ID_PROPERTY);
    catalogHelper.getStoreForContentExpression(contentExpression).loadValue(function():void{
      var catalogObject:CatalogObject = catalogHelper.getCatalogObject(externalIdExpression.getValue(), contentExpression) as CatalogObject;
      var urlString = catalogHelper.getImageUrl(catalogObject);
      if(urlString) {
        blob = urlString;
      }
    });
    return blob;
  }

  /**
   * Helper for thumbnail property fields.
   * @param bindTo
   * @return
   */
  public static function imageValueExpression(bindTo:ValueExpression):ValueExpression {
    return ValueExpressionFactory.createFromFunction(function():String {return editorContext.getThumbnailUri(bindTo.getValue(), null, CatalogHelper.getInstance().getType(bindTo.getValue()));})
  }
}
}