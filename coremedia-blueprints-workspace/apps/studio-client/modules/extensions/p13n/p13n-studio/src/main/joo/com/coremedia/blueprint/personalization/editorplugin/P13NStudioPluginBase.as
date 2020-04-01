package com.coremedia.blueprint.personalization.editorplugin {
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.configuration.StudioPlugin;
import com.coremedia.cms.editor.sdk.IEditorContext;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.util.MetaStyleService;
import com.coremedia.cms.editor.sdk.util.ThumbnailResolverFactory;

public class P13NStudioPluginBase extends StudioPlugin {
  public function P13NStudioPluginBase(config:P13NStudioPlugin= null) {
    super(config);
  }

  override public function init(editorContext:IEditorContext):void {
    super.init(editorContext);

    editorContext.registerContentInitializer("CMP13NSearch", initP13NSearch);

    MetaStyleService.setDefaultStyleForMetaData(MetaStyleService.STYLE_GROUP_YELLOW, [
      'CMP13NSearch', 'CMP13NSearch'
    ]);

    //TODO: is this line needed?
    editorContext.registerThumbnailResolver(ThumbnailResolverFactory.create("CMSelectionRules", "defaultContent"));
  }

  private function initP13NSearch(content:Content):void {
    content.getProperties().set('documentType', 'Document_');
    editorContext.getContentInitializer("CMLocalized")(content);
  }
}
}
