package com.coremedia.blueprint.studio.forms {
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.premular.DocumentTabPanel;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

public class CMChannelFormBase extends DocumentTabPanel {

  /**
   * A value expression that evaluates to an object mapping
   * ids of root channels to site structure infos.
   *
   * @see com.coremedia.cms.editor.sdk.sites.Site
   */
  private static var rootChannelsExpression:ValueExpression;

  public function CMChannelFormBase(config:CMChannelForm = null) {
    super(config);
  }

  /**
   * Returns a value expression that evaluates to an object mapping
   * ids of root channels to site objects.
   *
   * @return the root channels expression
   *
   * @see com.coremedia.cms.editor.sdk.sites.Site
   */
  public static function getRootChannelsExpression():ValueExpression {
    if (!rootChannelsExpression) {
      rootChannelsExpression = ValueExpressionFactory.createFromFunction(computeRootChannels);
    }
    return rootChannelsExpression;
  }

  private static function computeRootChannels():Object {
    var sites:Array = editorContext.getSitesService().getSites();
    if (!sites) {
      return undefined;
    }
    var result:Object = {};
    for (var i:int = 0; i < sites.length; i++) {
      var site:Site = sites[i];
      var siteRootDocument:Content = site.getSiteRootDocument();
      if (siteRootDocument) {
        result[siteRootDocument.getId()] = site;
      }
    }
    return result;
  }

  /**
   * Returns a value expression that checks if the content passed
   * in the given value expression is a root channel.
   * @param bindTo The value expression that contains the channel content.
   * @return A value expression that contains true if the given channel is an immediate child of CMSite.
   */
  public static function getIsRootChannelValueExpression(bindTo:ValueExpression):ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():Boolean {
      var content:Content = bindTo.getValue() as Content;
      if (content === undefined) {
        return undefined;
      }
      if (!content) {
        return false;
      }
      var rootChannels:* = getRootChannelsExpression().getValue();
      if (rootChannels == undefined) {
        return undefined;
      }
      return !!rootChannels[content.getId()];
    });
  }
}
}
