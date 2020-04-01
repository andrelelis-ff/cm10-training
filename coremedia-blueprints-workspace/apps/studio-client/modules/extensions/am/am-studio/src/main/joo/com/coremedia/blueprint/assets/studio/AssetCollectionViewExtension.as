package com.coremedia.blueprint.assets.studio {

import com.coremedia.blueprint.assets.studio.repository.AssetRepositoryListContainer;
import com.coremedia.blueprint.assets.studio.repository.AssetSearchListContainer;
import com.coremedia.blueprint.assets.studio.search.ExpirationDateFilterPanel;
import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.ContentTypeNames;
import com.coremedia.cap.content.search.SearchParameters;
import com.coremedia.cms.editor.sdk.ContentTreeRelation;
import com.coremedia.cms.editor.sdk.collectionview.RepositoryCollectionViewExtension;
import com.coremedia.cms.editor.sdk.collectionview.search.ContentTypeSelectorBase;
import com.coremedia.cms.editor.sdk.collectionview.search.LastEditedFilterPanel;
import com.coremedia.cms.editor.sdk.collectionview.search.SearchQueryUtil;
import com.coremedia.cms.editor.sdk.collectionview.search.StatusFilterPanel;
import com.coremedia.cms.editor.sdk.editorContext;

public class AssetCollectionViewExtension extends RepositoryCollectionViewExtension {
  public static const INSTANCE_NAME:String = "assets";

  [ArrayElementType("Object")]
  private var availableSearchTypes:Array;
  private var assetTreeRelation:ContentTreeRelation;

  public function AssetCollectionViewExtension() {
    this.availableSearchTypes = computeAvailableSearchTypes();
    assetTreeRelation = new AssetTreeRelation();
  }

  override public function isUploadDisabledFor(folder:Object):Boolean {
    return true;
  }

  override public function getContentTreeRelation():ContentTreeRelation {
    return assetTreeRelation;
  }

  override public function isApplicable(model:Object):Boolean {
    var content:Content = model as Content;
    if (!content) {
      return false;
    }

    var path:String = content.getPath();
    if (path === undefined) {
      return undefined;
    }

    if (path) {
      return path.indexOf(AssetConstants.ASSET_LIBRARY_PATH) === 0;
    }

    return false;
  }

  override public function getEnabledSearchFilterIds(model:Object):Array {
    return [
      StatusFilterPanel.FILTER_ID,
      LastEditedFilterPanel.FILTER_ID,
      'datefilter-panel-modificationdate',
      'datefilter-panel-publicationdate',
      'Location',
      'Subject',
      'Asset Download Portal',
      'rightsChannels',
      'rightsRegions',
      ExpirationDateFilterPanel.FILTER_ID
    ];
  }

  override public function getAvailableSearchTypes(folder:Object):Array {
    return availableSearchTypes;
  }

  override public function applySearchParameters(folder:Content, filterQueryFragments:Array, searchParameters:SearchParameters):SearchParameters {
    var assetDocTypeNames:Array = AssetDoctypeUtil.getAllAssetContentTypeNames();

    var docTypeExclusions:Array = editorContext.getContentTypesExcludedFromSearchResult().filter(
            function (docType:String):Boolean {
              return assetDocTypeNames.indexOf(docType) === -1;
            });

    var excludeDoctypesQuery:String = SearchQueryUtil.buildExcludeContentTypesQuery(docTypeExclusions);
    searchParameters.filterQuery = [filterQueryFragments.join(" AND ")];
    searchParameters.filterQuery.push(excludeDoctypesQuery);

    return searchParameters;
  }

  override public function getFolderContainerItemId():String {
    return AssetRepositoryListContainer.ITEM_ID;
  }

  override public function getSearchViewItemId():String {
    return AssetSearchListContainer.ITEM_ID;
  }

  private static function computeAvailableSearchTypes():Array {
    var assetDocTypes:Array = SESSION.getConnection().getContentRepository().getDocumentTypes().filter(
            function (contentType:ContentType):Boolean {
              return contentType.getName() === ContentTypeNames.DOCUMENT ||
                      contentType.isSubtypeOf(AssetConstants.DOCTYPE_ASSET);
            });

    return ContentTypeSelectorBase.getAvailableContentTypeEntries(assetDocTypes);
  }
}
}
