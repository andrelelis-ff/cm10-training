package com.coremedia.blueprint.studio {

import com.coremedia.blueprint.base.components.util.UserUtil;
import com.coremedia.blueprint.studio.taxonomy.TaxonomyUtil;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.configuration.StudioPlugin;
import com.coremedia.cms.editor.sdk.IEditorContext;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.preview.PreviewURI;
import com.coremedia.cms.editor.sdk.util.StringHelper;
import com.coremedia.ui.data.ValueExpressionFactory;

public class TaxonomyStudioPluginBase extends StudioPlugin {

  public function TaxonomyStudioPluginBase(config:TaxonomyStudioPlugin = null) {
    super(config);
  }

  override public function init(editorContext:IEditorContext):void {
    super.init(editorContext);
    editorContext.registerPreviewUrlTransformer(function (uri:PreviewURI, callback:Function):void {
      var source:Content = uri.getSource() as Content;
      ValueExpressionFactory.createFromFunction(TopicsHelper.resolveTaxonomyForTopicPage, source).loadValue(
              function (taxonomy:Content):void {
                if (taxonomy) {
                  uri.appendParameter("taxonomyId", IdHelper.parseContentId(taxonomy));
                }
                callback.call(null);
              }
      );
    });

    addSearchFilters();
  }

  /**
   * Returns true if the current user can administrate the taxonomies.
   * @return
   */
  public static function isAdministrationEnabled(callback:Function):void {
    //initially requesting if the admin tab is enabled
    TaxonomyUtil.loadSettings(function (adminGroups:Array):void {
      if (SESSION.getUser().isAdministrative()) {
        callback.call(null, true);
      } else {
        for (var i:int = 0; i < adminGroups.length; i++) {
          var groupName:String = StringHelper.trim(adminGroups[i], '');
          if (UserUtil.isInGroup(groupName)) {
            callback.call(null, true);
            return;
          }
        }
        callback.call(null, false);
      }
    });
  }

  private static function addSearchFilters():void {
    editorContext.getEnabledSearchFilterIds().push('Location', 'Subject');
  }
}
}
