package com.coremedia.livecontext.studio.library {
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cms.editor.sdk.RepositoryContentTreeRelation;

/**
 * Content Tree relation that cares only for augmented categories.
 */
public class LivecontextContentTreeRelation extends RepositoryContentTreeRelation {

  override public function folderNodeType():String {
    return "CMExternalChannel";
  }

  override public function leafNodeType():String {
    return "CMExternalChannel";
  }

  override public function mayCopy(contents:Array, newParent:Content):Boolean {
    return false;
  }

  override public function mayMove(contents:Array, newParent:Content):Boolean {
    return false;
  }

  override public function mayCreate(folder:Content, contentType:ContentType):Boolean {
    return false;
  }

  override public function showInTree(contents:Array, view:String = null, treeModelId:String = null):void {
    new ShowInCatalogTreeHelper(contents).showItems(treeModelId);
  }

}
}