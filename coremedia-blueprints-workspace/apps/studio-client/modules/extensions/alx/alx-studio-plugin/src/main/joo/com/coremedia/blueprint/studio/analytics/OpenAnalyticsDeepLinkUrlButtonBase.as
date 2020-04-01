package com.coremedia.blueprint.studio.analytics {
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;

import ext.button.Button;

public class OpenAnalyticsDeepLinkUrlButtonBase extends OpenAnalyticsUrlButtonBase {

  internal static const NO_PREVIEW_TYPES:Array = editorContext.getDocumentTypesWithoutPreview();

  [Bindable]
  public var contentExpression:ValueExpression;

  private var uriExpression:ValueExpression;

  public function OpenAnalyticsDeepLinkUrlButtonBase(config:OpenAnalyticsDeepLinkUrlButton = null) {
    super(config);
    const localBean:Bean = beanFactory.createLocalBean();

    uriExpression = ValueExpressionFactory.create('serviceUrl', localBean);

    getAlxServiceBean();
    contentExpression.addChangeListener(getAlxServiceBean);
  }

  public function setContent(content:Content):void {
    contentExpression.setValue(content);
  }

  override internal function initUrlValueExpression():void {
    this['urlValueExpression'] = getAlxReportUrl();
  }

  private function getAlxServiceBean():void {
    if (contentExpression && contentExpression.getValue()) {
      const content:Content = contentExpression.getValue();
      // if content has no preview, it cannot be rendered by CAE and thus has no report URL link either
      const nameValueExpression:ValueExpression = ValueExpressionFactory.create("type.name", content);
      nameValueExpression.loadValue(function (typeName:String):void {
        if (NO_PREVIEW_TYPES.indexOf(typeName) < 0) {
          var id:int = IdHelper.parseContentId(content);
          uriExpression.setValue("alxservice/" + id);
        }
      });
    }
  }

  protected function getAlxReportUrl():ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():String {
      const uri:String = uriExpression && uriExpression.getValue(); // uriExpression maybe null
      if (typeof(uri) === "string") {
        const remoteBean:RemoteBean = beanFactory.getRemoteBean(uri);
        if (remoteBean) {
          return ValueExpressionFactory.create(serviceName, remoteBean).getValue();
        }
      }
      return null;
    }, serviceName);
  }

}
}
