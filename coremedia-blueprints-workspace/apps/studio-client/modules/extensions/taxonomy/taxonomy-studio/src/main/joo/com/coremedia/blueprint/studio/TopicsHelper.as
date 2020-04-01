package com.coremedia.blueprint.studio {
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.impl.RemoteServiceMethod;
import com.coremedia.ui.data.impl.RemoteServiceMethodResponse;

import ext.Ext;

/**
 * Provides helper methods processing topic pages, like reloading the topics.
 */
public class TopicsHelper {

  /**
   * Loads all topics for the given taxonomy.
   * @param taxonomy The id of the taxonomy folder
   * @param siteId The id of the preferred site
   * @param term The search term to filter for or null/undefined.
   * @param callback The callback that contains the topics.
   */
  public static function loadTopics(taxonomy:Number, siteId:String, term:String, callback:Function):void {
    var params:String = Ext.urlEncode({taxonomy:taxonomy, site:siteId, term:term});
    var entriesBean:RemoteBean = beanFactory.getRemoteBean('topicpages/topics?' + params);
    entriesBean.invalidate(function ():void {
      callback.call(null, entriesBean.get('items'), entriesBean.get('filtered'));
    });
  }

  /**
   * Loads the topic page settings.
   * @param callback
   */
  public static function loadSettings(callback:Function):void {
    var url:String = 'topicpages/settings';
    var preferredSiteId:String = editorContext.getSitesService().getPreferredSiteId();
    if(preferredSiteId) {
      url+=('?'+Ext.urlEncode({site: preferredSiteId}));
    }
    var settingsRemoteBean:RemoteBean = beanFactory.getRemoteBean(url);
    settingsRemoteBean.invalidate(function ():void {
      callback.call(null, settingsRemoteBean);
    });
  }

  /**
   * Updates the default page or the custom page for the given topic.
   * @param id The id of the topic to update the context link for.
   * @param site The site to update the page for
   * @param create determines, if this site should be created or not
   * @param callback The callback that contains the updated topic representation.
   */
  public static function updatePage(id:Number, site:String, create:Boolean, callback:Function):void {
    var url:String = 'topicpages/page';
    new RemoteServiceMethod(url, "POST").request({
      id:id,
      site:site,
      create:create
    }, function (response:RemoteServiceMethodResponse):void {
      var json:* = response.getResponseJSON();
      callback.call(null, json);
    }, null);
  }

  /**
   * Tries to resolve the taxonomy if the active content is a Channel and a custom topic page
   * @param c
   * @return the taxonomy content (or undefined if not loaded)
   */
  public static function resolveTaxonomyForTopicPage(c:Content):Content {
    if (c) {
      var contentType:ContentType = c.getType();
      if(undefined === contentType) {
        return undefined;
      }
      if (contentType.getName() === "CMChannel") {
        var items:Array = c.getReferrers();
        if(undefined === items) {
          return undefined;
        }
        for (var i:int = 0; i < items.length; i++) {
          var ref:Content = items[i];
          if(ref && !ref.getState().readable) {
            continue;
          }
          if (undefined === ref.getType()) {
            return undefined;
          }
          if(ref.getType().isSubtypeOf("CMTaxonomy")) {
            return ref;
          }
        }
      }
    }
    return null;
  }

}
}