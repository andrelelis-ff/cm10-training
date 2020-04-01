package com.coremedia.livecontext.studio {
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.util.ImageLinkListRenderer;
import com.coremedia.cms.editor.sdk.util.ThumbnailResolverImpl;

/**
 * Catalog content thumbnails not necessarily based on blobs but could be
 * external URLs too.
 */
public class CatalogTeaserThumbnailResolver extends CatalogThumbnailResolver {

  public function CatalogTeaserThumbnailResolver(docType:String) {
    super(docType);
  }

  override public function getThumbnail(model:Object, operations:String = null):Object {
    return renderLiveContextProductTeaserPreview(model as Content);
  }

  private function renderLiveContextProductTeaserPreview(content:Content):Object {
    //manually build the lookup path since we can not access the editorContext which would result in a stackoverflow
    var resolver:ThumbnailResolverImpl = new ThumbnailResolverImpl();
    resolver.addMapping("CMProductTeaser", "pictures");
    resolver.addMapping("CMPicture", "data");
    var result:Object = resolver.getThumbnail(content, ImageLinkListRenderer.DEFAULT_CROPPING);
    if(result === undefined) {
      return undefined;
    }

    if(!result){
      return renderLiveContextPreview(content);
    }

    return result;
  }
}
}