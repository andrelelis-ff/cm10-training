package com.coremedia.blueprint.elastic.social.studio.curated {

import com.coremedia.cap.content.Content;
import com.coremedia.cap.undoc.content.ContentUtil;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.elastic.social.studio.model.ModeratedItem;
import com.coremedia.elastic.social.studio.model.impl.ModerationImpl;
import com.coremedia.ui.data.impl.RemoteServiceMethod;
import com.coremedia.ui.data.impl.RemoteServiceMethodResponse;

public class CuratedUtil {
  /**
   * Create an Article and make a {@link RemoteServiceMethod} request to curate that article with the selected comments.
   * @param content the Content that is being created via the openCreateContentDialog
   */
  public static function postCreateArticleFromComments(content:Content):void {
    var remoteServiceMethod:RemoteServiceMethod = new RemoteServiceMethod((ModerationImpl.getInstance() as ModerationImpl).getTenantAwareESUriPrefix() + "/curate/comments", 'POST');
    var params:* = makeRequestParameters(content);
    remoteServiceMethod.request(params, openOnSuccessfullyCreatedArticle, null);
  }

  /**
   * Creates an image gallery from comments with pictures using a {@link RemoteServiceMethod} request.
   * @param content the Content that is being created via the openCreateContentDialog
   */
  public static function postCreateGalleryFromComments(content:Content):void {
    var remoteServiceMethod:RemoteServiceMethod = new RemoteServiceMethod((ModerationImpl.getInstance() as ModerationImpl).getTenantAwareESUriPrefix() + "/curate/images", 'POST');
    var params:* = makeRequestParameters(content);
    remoteServiceMethod.request(params, openOnSuccessfullyCreatedArticle, null);
  }

  /**
   *
   * @param content the Content that is being created via the openCreateContentDialog.
   * @return a plain JS Object that holds the capId and an array of commentIds that needs to be curated
   */
  private static function makeRequestParameters(content:Content):Object {
    return {
      capId:content.getId(),
      commentIds:getCommentIds()
    }
  }

  /**
   * Open the created Article when everything is ok.
   * @param response the RemoteServiceMethodResponse which is provided at the request
   */
  private static function openOnSuccessfullyCreatedArticle(response:RemoteServiceMethodResponse):void {
    var id:String = response.response.responseText;
    var content:Content = ContentUtil.getContent(id);
    content.invalidate(function ():void {
      editorContext.getContentTabManager().openDocument(content);
      ModerationImpl.getInstance().getArchiveContributionAdministration().invalidateDisplayed();
    });
  }

  /**
   * Get the Comment IDs of each comment in a single string, separated by ";".
   * @return String Get the Comment IDs of each comment in a single string, separated by ";"
   */
  private static function getCommentIds():String {
    var commentArray:Array = ModerationImpl.getInstance().getArchiveContributionAdministration().getSelectedContributionItems();
    var commentIdsAsString:String = "";

    for (var i:Number = 0; i < commentArray.length; i++) {
      var commentId:String = (commentArray[i] as ModeratedItem).getTargetId();
      commentIdsAsString = commentIdsAsString.concat(commentId + ";");
    }
    return commentIdsAsString;
  }
}
}
