package com.coremedia.blueprint.studio.esanalytics {
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;

public class EsAnalyticsImpl {
  public static const TENANT_URI_SEGMENT:String = "tenant";
  public static const ES_ALX_CHART:String = "esAlxChartItemId";

  public static const ELASTIC_API_BASE_URL:String = "elastic/";
  public static const ALX_API_BASE_URL:String = ELASTIC_API_BASE_URL + "alx/";
  private static const ALX_PAGEVIEWS_API_BASE_URL:String = "pageviews/";
  private static const PUBLICATIONS_API_BASE_URL:String = "publications/";

  public static function getAlxPageViews(tenant:String, propertyName:String, contentId:String, timeRange:String):ValueExpression {
    var pageviewsUriPrefix:String = getTenantAwareAlxPageviewsUriPrefix(tenant);
    if (pageviewsUriPrefix) {
      return ValueExpressionFactory.create(propertyName, beanFactory.getRemoteBean(pageviewsUriPrefix + convertIdField(contentId)
              + "?timeRange=" + timeRange));
    }
    return null;
  }

  public static function getPublicationData(tenant:String, propertyName:String, contentId:String, timeRange:String):ValueExpression {
    var publicationsUriPrefix:String = getTenantAwarePublicationsUriPrefix(tenant);
    if (publicationsUriPrefix) {
      return ValueExpressionFactory.create(propertyName, beanFactory.getRemoteBean(publicationsUriPrefix + convertIdField(contentId)
              + "?timeRange=" + timeRange));
    }
  }

  public static function convertIdField(id:String):String {
    return id.substr(id.lastIndexOf('/') + 1, id.length);
  }

  private static function getTenantAwareAlxUriPrefix(tenant:String):String {
    return tenant + "/" + ALX_API_BASE_URL;
  }

  private static function getTenantAwareAlxPageviewsUriPrefix(tenant:String):String {
    if (tenant) {
      return getTenantAwareAlxUriPrefix(tenant) + ALX_PAGEVIEWS_API_BASE_URL;
    }
    return null;
  }


  private static function getTenantAwarePublicationsUriPrefix(tenant:String):String {
    if (tenant) {
      return getTenantAwareAlxUriPrefix(tenant) + PUBLICATIONS_API_BASE_URL;
    }
    return null;
  }
}
}