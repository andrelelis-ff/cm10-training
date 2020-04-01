package com.coremedia.blueprint.elastic.social.studio {
import com.coremedia.cms.editor.configuration.StudioPlugin;
import com.coremedia.cms.editor.sdk.IEditorContext;
import com.coremedia.cms.editor.sdk.util.MetaStyleService;

public class ElasticSocialStudioPluginBase extends StudioPlugin{

  public function ElasticSocialStudioPluginBase(config:ElasticSocialStudioPlugin = null) {
    super(config);
  }

  override public function init(editorContext:IEditorContext):void {
    super.init(editorContext);

    // Colorful Studio styles
    MetaStyleService.setDefaultStyleForMetaData(MetaStyleService.STYLE_GROUP_YELLOW, ['ESDynamicList']);
  }

}
}
