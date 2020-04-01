package com.coremedia.blueprint.assets.studio {

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.impl.ContentRepositoryImpl;
import com.coremedia.cms.editor.sdk.RepositoryContentTreeRelation;

public class AssetTreeRelation extends RepositoryContentTreeRelation {

  override public function mayCreate(folder:Content, contentType:ContentType):Boolean {
    var mayCreate:Boolean = super.mayCreate(folder, contentType);
    if(mayCreate === undefined) {
      return undefined;
    }
    if(!mayCreate) {
      return false;
    }

    return contentType.isSubtypeOf(ContentRepositoryImpl.FOLDER_CONTENT_TYPE) ||
            contentType.isSubtypeOf(AssetConstants.DOCTYPE_ASSET);
  }

  override public function mayMove(sources:Array, newParent:Content):Boolean {
    return super.mayMove(sources, newParent) && mayMoveOrCopyToAssetLibrary(sources);
  }

  override public function mayCopy(sources:Array, newParent:Content):Boolean {
    return super.mayCopy(sources, newParent) && mayMoveOrCopyToAssetLibrary(sources);
  }

  private static function mayMoveOrCopyToAssetLibrary(sources:Array):Boolean {
    for each(var content:Content in sources) {
      if (content.getPath() === undefined) {
        return undefined;
      }

      var path:String = content.getPath();
      if (path.indexOf(AssetConstants.ASSET_LIBRARY_PATH) !== 0) {
        return false;
      }
    }
    return true;
  }
}
}