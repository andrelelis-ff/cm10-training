package com.coremedia.blueprint.studio.esanalytics {

import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.desktop.WorkArea;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.premular.DocumentTabPanel;
import com.coremedia.cms.editor.sdk.premular.PropertyFieldGroup;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.impl.RemoteServiceMethod;
import com.coremedia.ui.data.impl.RemoteServiceMethodResponse;

import ext.data.Model;
import ext.grid.GridPanel;
import ext.panel.Panel;

public class EsAnalyticsChartPanelBase extends PropertyFieldGroup {

  protected var timeRangeValueExpression:ValueExpression;

  private var esChart:EsChart;
  private var tenantVE:ValueExpression;

  public function EsAnalyticsChartPanelBase(config:EsAnalyticsChartPanel = null) {
    super(config);
  }

  protected function getTimeRangeValueExpression():ValueExpression {
    if (!timeRangeValueExpression) {
      timeRangeValueExpression = ValueExpressionFactory.create('timerange', beanFactory.createLocalBean({'timerange':7}));
    }
    return timeRangeValueExpression;
  }

  override protected function afterRender():void {
    super.afterRender();

    var systemTabPanel:Panel = this.findParentByType(DocumentTabPanel) as Panel;
    var versionHistoryListView:GridPanel = systemTabPanel.queryById('versionHistory') as GridPanel;
    if(versionHistoryListView) {
      mon(versionHistoryListView, 'mouseenter', markEventInChartPanel);
    }
  }

  private function markEventInChartPanel(historyPanel:GridPanel, index:Number):void {
    if (getEsChart().getLineChart()) {
      var record:Model = historyPanel.getStore().getAt(index);
      var lifecycleStatus:String = record.data.lifecycleStatus as String;
      if ("published" === lifecycleStatus) {
        var date:Date = record.data.editionDate as Date;
        var now:Date = new Date();
        var today:Date = new Date(now.getFullYear(), now.getMonth(), now.getDate());
        var diff:Number = Math.ceil((today.getTime() - date.getTime()) / 86400000); //in days
        var interval:Number = getEsChart().getLineChartData().length;
        if (interval - diff >= 0) {
          var pos:Number = interval - diff;
          getEsChart().getLineChart().displayHoverForPublication(pos - 1);
        }
      }
    }
  }

  protected static function getCurrentContent():Content {
    return WorkArea.ACTIVE_CONTENT_VALUE_EXPRESSION.getValue() as Content;
  }

  private function getEsChart():EsChart {
    if (!esChart) {
      esChart = this.queryById(EsAnalyticsChart.ES_CHART_ITEM_ID) as EsChart;
    }
    return esChart;
  }

  public function getAlxData(propertyName:String):ValueExpression {
    return ValueExpressionFactory.createFromFunction(function (propertyName1:String):RemoteBean {
      var currentContent:Content = getCurrentContent();
      if (validContent(currentContent) && getTenantVE() && getTenantVE().getValue()) {
        var alxPageViewsVE:ValueExpression = EsAnalyticsImpl.getAlxPageViews(getTenantVE().getValue(),
                propertyName1, currentContent.getId(), getTimeRangeValueExpression().getValue());
        if (alxPageViewsVE) {
          return alxPageViewsVE.getValue();
        }
      }
      // must not be undefined to trigger the BindPropertyPlugin in ExChart
      return null;
    }, propertyName);
  }

  private static function validContent(content:Content):Boolean {
    return content && content.getPath();
  }

  private function getTenantVE():ValueExpression {
    if (!tenantVE) {
      tenantVE = ValueExpressionFactory.createFromValue(undefined);

      if (getCurrentContent()) {
        var site:Site = editorContext.getSitesService().getSiteFor(getCurrentContent());
        if (site) {
          var siteId:String = site.getId();

          var rsm:RemoteServiceMethod = new RemoteServiceMethod(EsAnalyticsImpl.ELASTIC_API_BASE_URL + EsAnalyticsImpl.TENANT_URI_SEGMENT + "?siteId=" + siteId, "GET");
          rsm.request(null,
                  function (response:RemoteServiceMethodResponse):void {
                    var tenantInfo:Object = response.getResponseJSON();
                    tenantVE.setValue(tenantInfo['tenant']);
                  }
          );
        }
      }
    }
    return tenantVE;
  }
}
}
