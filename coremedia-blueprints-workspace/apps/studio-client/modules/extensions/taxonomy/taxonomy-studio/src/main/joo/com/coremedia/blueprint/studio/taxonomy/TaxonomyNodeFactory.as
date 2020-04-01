package com.coremedia.blueprint.studio.taxonomy {
import com.coremedia.blueprint.studio.TaxonomyStudioPlugin;
import com.coremedia.blueprint.studio.taxonomy.preferences.TaxonomyPreferencesBase;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentPropertyNames;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;

import ext.Ext;

public class TaxonomyNodeFactory {

  public static function loadTaxonomies(site:String, callback:Function, reload:Boolean = true):void {
    var url:String = "taxonomies/roots";
    url = appendParam(url, 'site', site);
    url = appendParam(url, 'reload', reload);
    loadRemoteTaxonomyNodeList(url, reload, callback);
  }

  public static function loadTaxonomyRoot(site:String, taxonomyId:String, callback:Function) {
    var url:String = "taxonomies/root?" + Ext.urlEncode({taxonomyId:taxonomyId});
    if(site) {
      url+="&site=" + site;
    }
    var remote:RemoteBean = beanFactory.getRemoteBean(url);
    remote.load(function():void {
      var obj = remote.toObject();
      if(!obj.type || !obj.name) {
        trace('[INFO]', 'No taxonomy found for site "' + site + '" and taxonomy id "' + taxonomyId + '"');
        callback.call(null, null);
      }
      else {
        var reloadedNode = new TaxonomyNode(obj);
        callback(reloadedNode);
      }
    });
  }

  public static function loadPath(taxonomyId:String, ref:String, siteId:String, callback:Function):void {
    var url:String = "taxonomies/path?" + Ext.urlEncode({taxonomyId:taxonomyId, nodeRef:ref, site:siteId});
    var entriesBean:RemoteBean = beanFactory.getRemoteBean(url);
    entriesBean.invalidate(function ():void {
      if(entriesBean.get("path")) { //null if another root node was returned as default.
        var nodelist:TaxonomyNodeList = new TaxonomyNodeList(entriesBean.get("path")["nodes"]);
        callback(nodelist);
      }
    });
  }

  public static function loadRemoteTaxonomyNodeList(url:String, refresh:Boolean, callback:Function) {
    // create a remote bean which
    var entriesBean:RemoteBean = beanFactory.getRemoteBean(url);
    if(refresh) {
      entriesBean.invalidate(function ():void {
        var nodelist:TaxonomyNodeList = new TaxonomyNodeList(entriesBean.get("nodes"));
        callback(nodelist);
      });
    }
    else {
      entriesBean.load(function ():void {
        var nodelist:TaxonomyNodeList = new TaxonomyNodeList(entriesBean.get("nodes"));
        callback(nodelist);
      });
    }
  }

  public static function loadSuggestions(taxonomyId:String, document:Content, callback:Function):void {
    var id:String = document.getId();
    var valueString:String = editorContext.getPreferences().get(TaxonomyPreferencesBase.PREFERENCE_SEMANTIC_SETTINGS_KEY);
    if (!valueString) {
      valueString = TaxonomyStudioPlugin.TAXONOMY_NAME_MATCHING_KEY;
    }
    var semanticService:String = valueString;
    var remoteBeanUrl:String = "taxonomies/suggestions?" + Ext.urlEncode({taxonomyId:taxonomyId, semanticStrategyId:semanticService, id:id, max:20});
    ValueExpressionFactory.create(ContentPropertyNames.PATH, document).loadValue(function():void {
      var siteId:String = editorContext.getSitesService().getSiteIdFor(document);
      if (siteId) {
        remoteBeanUrl += "&site=" + siteId;
      }
      var remoteBean:RemoteBean = beanFactory.getRemoteBean(remoteBeanUrl);
      remoteBean.invalidate(function ():void {
        var nodelist:TaxonomyNodeList = new TaxonomyNodeList(remoteBean.get("nodes"));
        callback(nodelist);
      });
    });
  }
  
  private static function appendParam(url:String, name:String, value:*):String {
    if(!value) {
      return url;
    }
    
    if(url.indexOf('?') === -1) {
      url = url + "?" + name + "=" + value;
    }
    else {
      url = url + "&" + name + "=" + value;
    }

    return url;
  }
}

}
