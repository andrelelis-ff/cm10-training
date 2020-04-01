package com.coremedia.blueprint.assets.studio {

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.ContentTypeNames;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cms.editor.configuration.StudioPlugin;
import com.coremedia.cms.editor.sdk.EditorContextImpl;
import com.coremedia.cms.editor.sdk.IEditorContext;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewConstants;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewManagerInternal;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewModel;
import com.coremedia.cms.editor.sdk.collectionview.SearchState;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.preview.PreviewPanel;
import com.coremedia.cms.editor.sdk.util.MetaStyleService;
import com.coremedia.cms.editor.sdk.util.ThumbnailResolverFactory;

import mx.resources.ResourceManager;

[ResourceBundle('com.coremedia.blueprint.assets.studio.AMStudioPlugin')]
public class AMStudioPluginBase extends StudioPlugin {
  private static const SHOW_PROPERTY:String = "show";

  // if false, no rendition is downloadable per default
  // if true, all renditions are downloadable per default
  //noinspection JSFieldCanBeLocal
  private static const DEFAULT_SHOW_VALUE:Boolean = false;

  public function AMStudioPluginBase(config:AMStudioPlugin = null) {
    super(config);
  }

  override public function init(editorContext:IEditorContext):void {
    super.init(editorContext);

    editorContext.registerThumbnailResolver(ThumbnailResolverFactory.create(AssetConstants.DOCTYPE_ASSET, AssetConstants.PROPERTY_ASSET_THUMBNAIL));

    editorContext.registerContentInitializer(AssetConstants.DOCTYPE_PICTURE_ASSET, initAMPictureAsset);
    editorContext.registerContentInitializer(AssetConstants.DOCTYPE_VIDEO_ASSET, initAMVideoAsset);

    var contentRepository:ContentRepository = editorContext.getSession().getConnection().getContentRepository();
    contentRepository.getChild(AssetConstants.ASSET_LIBRARY_PATH,
            function (assetsFolder:Content):void {
              if (assetsFolder) {
                assetsFolder.load(function ():void {
                  if (assetsFolder.getState().readable) {
                    var collectionViewManager:CollectionViewManagerInternal =
                            CollectionViewManagerInternal(editorContext.getCollectionViewManager());
                    collectionViewManager.addRepositoryTreeRoot(assetsFolder,
                            ResourceManager.getInstance().getString('com.coremedia.blueprint.assets.studio.AMStudioPlugin', 'CollectionView_assetRootFolder_icon'));
                  }
                });
              }
            });

    addAssetCollectionViewExtension();
    removeAssetDoctypesByDefault();

    // Colorful Studio type
    MetaStyleService.setDefaultStyleForMetaData(MetaStyleService.STYLE_GROUP_GREEN, [
      AssetConstants.DOCTYPE_PICTURE_ASSET, AssetConstants.DOCTYPE_DOCUMENT_ASSET, AssetConstants.DOCTYPE_VIDEO_ASSET]);
  }

  protected static function reloadAssetPreview(previewPanel:PreviewPanel):void {
    if (previewPanel.rendered && isAssetContent(previewPanel.getCurrentPreviewContentValueExpression().getValue() as Content)) {
      previewPanel.reloadFrame();
    }
  }

  private static function addAssetCollectionViewExtension():void {
    editorContext.getCollectionViewExtender().addExtension(new AssetCollectionViewExtension(), 500);
  }

  private static function removeAssetDoctypesByDefault():void {
    AssetDoctypeUtil.getAllAssetContentTypeNames().forEach(removeDoctype);
  }

  private static function removeDoctype(contentTypeName:String):void {
    addToArrayIfNotAlreadyContained(editorContext.getContentTypesExcludedFromSearch(), contentTypeName);
    addToArrayIfNotAlreadyContained(editorContext.getContentTypesExcludedFromSearchResult(), contentTypeName);
  }

  private static function addToArrayIfNotAlreadyContained(array:Array, item:*):void {
    if (array.indexOf(item) === -1) {
      array.push(item);
    }
  }

  private static function initAMAsset(content:Content):void {
    var original:Struct = createRendition(content, AssetConstants.PROPERTY_ASSET_ORIGINAL);
    setShowValue(original);
  }

  private static function initAMPictureAsset(content:Content):void {
    initAMAsset(content);
    var web:Struct = createRendition(content, AssetConstants.PROPERTY_ASSET_WEB);
    var print:Struct = createRendition(content, AssetConstants.PROPERTY_ASSET_PRINT);
    setShowValue(web);
    setShowValue(print);
  }

  private static function initAMVideoAsset(content:Content):void {
    initAMAsset(content);
    var web:Struct = createRendition(content, AssetConstants.PROPERTY_ASSET_WEB);
    setShowValue(web);
  }

  private static function createRendition(content:Content, rendition:String):Struct {
    var metadata:Struct = content.getProperties().get(AssetConstants.PROPERTY_ASSET_METADATA);
    metadata.getType().addStructProperty(AssetConstants.PROPERTY_ASSET_METADATA_RENDITIONS);
    var renditions:Struct = metadata.get(AssetConstants.PROPERTY_ASSET_METADATA_RENDITIONS);
    renditions.getType().addStructProperty(rendition);
    return renditions.get(rendition);
  }

  private static function setShowValue(rendition:Struct):void {
    rendition.set(SHOW_PROPERTY, DEFAULT_SHOW_VALUE);
  }

  private static function isAssetContent(content:Content):Boolean {
    if (!content) {
      return false;
    }
    return content.getType().isSubtypeOf(AssetDoctypeUtil.getAssetContentType());
  }

  internal static function mayCreate(selection:Content, contentType:ContentType):Boolean {
    if (!selection.getPath()) {
      return undefined;
    }

    return selection.getPath().indexOf(AssetConstants.ASSET_LIBRARY_PATH) === 0;
  }

  /**
   * Custom search handler for assets.
   * The collection manager must trigger the search under the Assets folder.
   */
  public static function openAssetSearch(linkListTargetType:ContentType, sourceContent:Content):void {
    var searchType:String = linkListTargetType.getName();

    //default supertype to all documents
    if (searchType === 'CMLinkable') {
      searchType = ContentTypeNames.DOCUMENT;
    }

    var contentRepository:ContentRepository = editorContext.getSession().getConnection().getContentRepository();
    contentRepository.getChild(AssetConstants.ASSET_LIBRARY_PATH, function (assetsRootFolder:Content):void {
      if (assetsRootFolder) {
        var collectionViewModel:CollectionViewModel = EditorContextImpl(editorContext).getCollectionViewModel();
        var state:SearchState = new SearchState();
        state.contentType = searchType;
        state.folder = assetsRootFolder;
        collectionViewModel.setSearchState(true, CollectionViewConstants.LIST_VIEW, state, assetsRootFolder);
        editorContext.getCollectionViewManager().openSearch(state, false, CollectionViewConstants.LIST_VIEW);
      }
    });
  }
}
}
