package com.coremedia.blueprint.studio.esanalytics {
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.dashboard.WidgetWrapper;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.impl.RemoteServiceMethod;
import com.coremedia.ui.data.impl.RemoteServiceMethodResponse;

import ext.container.Container;
import ext.panel.Panel;
import ext.toolbar.TextItem;
import ext.toolbar.Toolbar;

[ResourceBundle('com.coremedia.blueprint.studio.esanalytics.EsAnalyticsStudioPlugin')]
public class EsAnalyticsChartWidgetBase extends Container {

  /**
   * The content id of the root channel to show.
   */
  [Bindable]
  public var content:Content;

  private var tenantVE:ValueExpression;

  protected var timeRangeValueExpression:ValueExpression;

  public function EsAnalyticsChartWidgetBase(config:EsAnalyticsChartWidgetBase = null) {
    super(config);

    on("afterlayout", function ():void {
      var title:String = resourceManager.getString('com.coremedia.blueprint.studio.esanalytics.EsAnalyticsStudioPlugin', 'widget_title');
      if (config.content) {
        var content:Content = config.content;
        if (content) {
          content.load(function (cont:Content):void {
            getWidgetLabel().update(title + ": " + cont.getName());
          });
        }
      } else {
        getWidgetLabel().update(title + ": " + resourceManager.getString('com.coremedia.blueprint.studio.esanalytics.EsAnalyticsStudioPlugin', 'widget_title_channel_undefined'));
      }
    }, null, {single: true});
  }

  private function getWidgetLabel():TextItem {
    var wrapper:WidgetWrapper = findParentByType(WidgetWrapper.xtype) as WidgetWrapper;
    var innerWrapper:Panel = wrapper.queryById("innerWrapper") as Panel;
    var widgetToolbar:Toolbar = innerWrapper.getDockedItems("toolbar[dock=\"top\"]")[0] as Toolbar;
    return widgetToolbar.down("tbtext") as TextItem;
  }

  public function getAlxData(propertyName:String):ValueExpression {
    return ValueExpressionFactory.createFromFunction(function (propertyName1:String):Object {

      if (content && content.getId() && getTenantVE().getValue()) {
        var alxPageViewsVE:ValueExpression = EsAnalyticsImpl.getAlxPageViews(getTenantVE().getValue(),
                propertyName1, content.getId(), getTimeRangeValueExpression().getValue());
        if (alxPageViewsVE) {
          return alxPageViewsVE.getValue();
        }
      }
      return null;
    }, propertyName);
  }

  public function getPublicationData(propertyName:String):ValueExpression {
    return ValueExpressionFactory.createFromFunction(function (propertyName1:String):Object {
      if (content && content.getId() && getTenantVE().getValue()) {
        var publicationDataVE:ValueExpression = EsAnalyticsImpl.getPublicationData(getTenantVE().getValue(), propertyName1, content.getId(), getTimeRangeValueExpression().getValue());
        if (publicationDataVE) {
          return publicationDataVE.getValue();
        }
      }
      return null;
    }, propertyName);
  }

  protected function getTimeRangeValueExpression():ValueExpression {
    if (!timeRangeValueExpression) {
      timeRangeValueExpression = ValueExpressionFactory.create('timerange', beanFactory.createLocalBean({'timerange': 7}));
    }
    return timeRangeValueExpression;
  }

  private function getTenantVE():ValueExpression {
    if (!tenantVE) {
      tenantVE = ValueExpressionFactory.createFromValue(undefined);

      if (content) {
        content.load(function ():void {
          var site:Site = editorContext.getSitesService().getSiteFor(content);
          var tenantInfoUrl:String = EsAnalyticsImpl.ELASTIC_API_BASE_URL + EsAnalyticsImpl.TENANT_URI_SEGMENT + "?siteId=";
          // remote call can handle empty site id and would return the default tenant
          if (site) {
            tenantInfoUrl = tenantInfoUrl + EsAnalyticsImpl.convertIdField(site.getId());
          }
          var rsm:RemoteServiceMethod = new RemoteServiceMethod(tenantInfoUrl, "GET");
          rsm.request(null,
                  function (response:RemoteServiceMethodResponse):void {
                    var tenantInfo:Object = response.getResponseJSON();
                    tenantVE.setValue(tenantInfo['tenant']);
                  }
          );
        })
      }
    }
    return tenantVE;
  }
}
}