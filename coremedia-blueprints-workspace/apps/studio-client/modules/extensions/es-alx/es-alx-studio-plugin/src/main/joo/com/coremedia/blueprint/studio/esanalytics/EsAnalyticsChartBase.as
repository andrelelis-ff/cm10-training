package com.coremedia.blueprint.studio.esanalytics {
import com.coremedia.ui.data.ValueExpression;

import ext.panel.Panel;

public class EsAnalyticsChartBase extends Panel {

  [Bindable]
  public var bindTo:ValueExpression;

  [Bindable]
  public var timeRangeValueExpression:ValueExpression;

  private var chartPanel:EsChart;

  public function EsAnalyticsChartBase(config:EsAnalyticsChartBase = null) {
    super(config);
    mon(getChartPanel(), "resize", function():void {getChartPanel().initChartWhenAvailable()});
  }

  private function getChartPanel():EsChart {
    if (!chartPanel) {
      chartPanel = queryById(EsAnalyticsChart.ES_CHART_ITEM_ID) as EsChart
    }
    return chartPanel;
  }
}
}
