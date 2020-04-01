package com.coremedia.catalog.studio.collectionview.search {
import com.coremedia.catalog.studio.CatalogStudioPluginBase;
import com.coremedia.cms.editor.sdk.collectionview.search.ConditionalFilterPanel;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ecommerce.studio.components.preferences.CatalogPreferencesBase;
import com.coremedia.ecommerce.studio.helper.StoreUtil;
import com.coremedia.ecommerce.studio.model.Category;
import com.coremedia.ecommerce.studio.model.Store;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

public class LostandfoundFilterPanelBase extends ConditionalFilterPanel {
  public static const LOSTANDFOUND_CHECKBOX_SELECTED:String = "lostAndFoundCheckboxSelected";
  public static const DEFAULT_STATE:Object = {};
  DEFAULT_STATE[LOSTANDFOUND_CHECKBOX_SELECTED] = false;

  /**
   * The query fragment to be passed to Solr.
   */
  private static const FILTER_QUERY_LOSTANDFOUND:String = "(type:CMProduct OR type:CMCategory) AND NOT directProductCategories:[* TO *]";
  private var catalogRootExclusionsExpression:ValueExpression;
  private var catalogRootExclusions:String = "";


  public function LostandfoundFilterPanelBase(config:ConditionalFilterPanel = null) {
    super(config);
  }

  /**
   * @inheritDoc
   */
  override public function isApplicable():Boolean {
    return editorContext.getPreferences().get(CatalogPreferencesBase.PREFERENCE_SHOW_CATALOG_KEY);
  }

  /**
   * @inheritDoc
   */
  override public function doBuildQuery():String {
    var stateBean:Bean = getStateBean();
    var lostandfoundActive:Boolean = stateBean.get(LOSTANDFOUND_CHECKBOX_SELECTED);
    if (lostandfoundActive) {
      return FILTER_QUERY_LOSTANDFOUND + getCatalogRootExclusions();
    }
    return null;
  }

  /**
   * @inheritDoc
   */
  override public function getDefaultState():Object {
    return DEFAULT_STATE;
  }


  /**
   * The catalog root categories have no parents, but are not to be considered
   * as orphaned.  Exclude them in the query.
   */
  private function getCatalogRootExclusions():String {
    if (!catalogRootExclusionsExpression) {
      catalogRootExclusionsExpression = ValueExpressionFactory.createFromFunction(function ():String {
        var result:String = "";
        var storesExpression:ValueExpression = ValueExpressionFactory.createFromFunction(CatalogStudioPluginBase.findCoreMediaStores);
        var stores:Array = storesExpression.getValue();
        if (undefined === stores) {
          return undefined;
        }
        for each (var store:Store in stores) {
          var category:Category = StoreUtil.getRootCategoryForStoreExpression(store).getValue();
          if (undefined === category) {
            return undefined;
          }
          if (category) {
            var externalTechId:String = category.getExternalTechId();
            if (undefined === externalTechId) {
              return undefined;
            }
            result += " AND NOT numericid:" + externalTechId;
          }
        }
        return result;
      });
    }

    catalogRootExclusionsExpression.loadValue(function (exclusions:String):void {
      catalogRootExclusions = exclusions;
    });
    return catalogRootExclusions;
  }
}
}
