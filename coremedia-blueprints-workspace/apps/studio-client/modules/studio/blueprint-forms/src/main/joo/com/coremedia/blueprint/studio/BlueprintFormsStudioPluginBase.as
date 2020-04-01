package com.coremedia.blueprint.studio {

import com.coremedia.blueprint.base.components.sites.SiteAwareFeatureUtil;
import com.coremedia.blueprint.base.components.util.UserUtil;
import com.coremedia.blueprint.studio.forms.variants.PreviewVariantsUtil;
import com.coremedia.blueprint.studio.util.ContentInitializer;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cms.editor.configuration.StudioPlugin;
import com.coremedia.cms.editor.sdk.IEditorContext;
import com.coremedia.cms.editor.sdk.collectionview.CollectionView;
import com.coremedia.cms.editor.sdk.desktop.maintoolbar.MainNavigationToolbar;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.plugins.TabExpandPlugin;
import com.coremedia.cms.editor.sdk.premular.Premular;
import com.coremedia.cms.editor.sdk.preview.PreviewPanel;
import com.coremedia.cms.editor.sdk.util.MetaStyleService;
import com.coremedia.cms.editor.sdk.util.ThumbnailResolverFactory;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.dependencies.DependencyTracker;
import com.coremedia.ui.data.validation.Issue;
import com.coremedia.ui.data.validation.Issues;
import com.coremedia.ui.plugins.BindPropertyPlugin;

import ext.Ext;
import ext.button.Button;

public class BlueprintFormsStudioPluginBase extends StudioPlugin {

  private var contentTypeVE:ValueExpression;

  /**
   * suppress preview if content has issue with at least one of the codes listed here
   */
  internal static const ISSUE_CODES_WITHOUT_PREVIEW:Array = [
    'not_in_navigation'
  ];

  public function BlueprintFormsStudioPluginBase(config:BlueprintFormsStudioPlugin = null) {
    super(config);
  }

  override public function init(editorContext:IEditorContext):void {
    super.init(editorContext);

    //caches the available users and groups
    UserUtil.init();

    // Register Navigation Parent for CMChannel
    CMChannelExtension.register(CMChannelExtension.CONTENT_TYPE_PAGE);

    //Enable advanced tabs
    TabExpandPlugin.ADVANCED_TABS_ENABLED = true;

    /**
     * Globally turns off strict consistency checks for all BindPropertyPlugins where the config option
     * 'disableStrictConsistency' is not set explicitly.
     */
    BindPropertyPlugin.DISABLE_STRICT_CONSISTENCY = true;

    SiteAwareFeatureUtil.preLoadConfiguration();

    ContentInitializer.applyInitializers();

    MetaStyleService.setDefaultStyleForMetaData(MetaStyleService.STYLE_GROUP_TURQUOISE, ['CMTeaser', 'CMArticle']);
    MetaStyleService.setDefaultStyleForMetaData(MetaStyleService.STYLE_GROUP_GREEN, [
      'CMVideo', 'CMPicture', 'CMAudio', 'CMInteractive','CMDownload', 'CMImagemap', 'CMSpinner', 'CMSymbol', 'CMImage']);
    MetaStyleService.setDefaultStyleForMetaData(MetaStyleService.STYLE_GROUP_PURPLE, ['CMSitemap', 'CMSite', CMChannelExtension.CONTENT_TYPE_PAGE]);
    MetaStyleService.setDefaultStyleForMetaData(MetaStyleService.STYLE_GROUP_YELLOW, [
      'CMGallery', 'CMQueryList', 'CMCollection', 'CMSelectionRules']);
    MetaStyleService.setDefaultStyleForMetaData(MetaStyleService.STYLE_GROUP_BLUE, [MetaStyleService.FALLBACK_META_DATA]);
    MetaStyleService.setDefaultStyleForMetaData(MetaStyleService.STYLE_GROUP_NONE, ["folder_"]);

    editorContext.registerThumbnailResolver(ThumbnailResolverFactory.create("CMSelectionRules", "defaultContent"));
    editorContext.registerThumbnailResolver(ThumbnailResolverFactory.create("CMCollection", "pictures", "items"));
    editorContext.registerThumbnailResolver(ThumbnailResolverFactory.create("CMTeasable", "pictures"));
    editorContext.registerThumbnailResolver(ThumbnailResolverFactory.create("CMSpinner", "pictures", "sequence"));
    editorContext.registerThumbnailResolver(ThumbnailResolverFactory.create("CMDownload", "data"));
    editorContext.registerThumbnailResolver(ThumbnailResolverFactory.create("CMImage", "data"));
    editorContext.registerThumbnailResolver(ThumbnailResolverFactory.create("CMVideo", "pictures"));
    editorContext.registerThumbnailResolver(ThumbnailResolverFactory.create("CMPicture", "data"));
    editorContext.registerThumbnailResolver(ThumbnailResolverFactory.create("CMSymbol", "icon"));
    editorContext.registerThumbnailResolver(ThumbnailResolverFactory.create("CMTheme", "icon"));
  }

  /**
   * Add folder from library tree to quick create menu
   * @return String
   */
  public static function calculateQuickCreateFolder():String {
    var libToggleBtn:Button = Ext.getCmp(MainNavigationToolbar.LIBRARY_BUTTON_ITEM_ID) as Button;
    if (libToggleBtn) {
      DependencyTracker.dependOnObservable(libToggleBtn, "toggle");
    }
    var collectionView:CollectionView = Ext.getCmp(CollectionView.COLLECTION_VIEW_ID) as CollectionView;
    if (collectionView && collectionView.isVisible(true)) {
      var content:Content = collectionView.getSelectedFolderValueExpression().getValue() as Content;
      if (content) {
        return content.getPath();
      }
    }
    return undefined;
  }

  /**
   * Used for columns setup:
   * Extends the sort by sorting by name.
   *
   * @param field the sortfield which is selected and should be extended
   * @param direction the sortdirection which is selected
   * @return array filled with additional order by statements
   */
  public static function extendOrderByName(field:String, direction:String):Array {
    var orderBys:Array = [];
    orderBys.push('name ' + direction);
    return orderBys;
  }

  /**
   * Check if the given content is a CMLinkable and has no error issues.
   */
  protected function isValidCMLinkable(content:Content):Boolean {
    var contentType:ContentType = content.getType();
    if (contentType === undefined) {
      return undefined;
    }
    if (!contentType.isSubtypeOf("CMLinkable")) {
      return false;
    }
    var issues:Issues = content.getIssues();
    if (issues === undefined) {
      return undefined;
    }
    var all:Array = issues.getAll();
    if (all === undefined) {
      return undefined;
    }
    return !all.some(function (issue:Issue):Boolean {
      return ISSUE_CODES_WITHOUT_PREVIEW.indexOf(issue.code) > -1;
    });
  }

  protected static function reloadPreview(previewPanel:PreviewPanel):void {
    if (previewPanel.rendered && PreviewVariantsUtil.canHaveVariants(previewPanel.getCurrentPreviewContentValueExpression().getValue() as Content)) {
      previewPanel.reloadFrameWhenVisible();
    }
  }

  protected function getContentTypeVE():ValueExpression {
    if (!contentTypeVE) {
      contentTypeVE = ValueExpressionFactory.createFromFunction(function():String {
        var premular:Premular = editorContext.getWorkArea().getActiveTab() as Premular;
        if (!premular) {
          return null;
        }
        var currentContent:Content = premular.getContent();
        if (!currentContent) {
          return null;
        }
        if (!currentContent.getType()) {
          return undefined;
        }
        return currentContent.getType().getName();
      });
    }
    return contentTypeVE;
  }
}
}
