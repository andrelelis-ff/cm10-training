package com.coremedia.blueprint.assets.studio.search {

import com.coremedia.cms.editor.sdk.collectionview.search.*;
import com.coremedia.cms.editor.sdk.util.FormatUtil;

public class ExpirationDateFilterPanelBase extends FilterPanel {
  public static const KEY:String = 'key';
  public static const DATE:String = 'date';
  private static const SOLR_FIELD:String = 'expirationDate';

  public function ExpirationDateFilterPanelBase(config:ExpirationDateFilterPanel = null) {
    super(config);
  }

  override public function buildQuery():String {
    var key:String = getStateBean().get(KEY);
    if (!key || key === 'any') {
      return '';
    }

    if (key === 'byDate') {
      var date:Date = getStateBean().get(DATE);
      if (!date) {
        return '';
      }

      var dateAsISO8601:String = date['toISOString'](); // TODO: add to jangaroo as API - supported since IE9
      return FormatUtil.format('{0}:[* TO {1}]', SOLR_FIELD, dateAsISO8601);
    }

    var queryFormat:String = '';
    switch (key) {
      case 'inOneDay':
        queryFormat = '{0}:[* TO NOW/DAY+1DAY]';
        break;
      case 'inOneWeek':
        queryFormat = '{0}:[* TO NOW/DAY+7DAYS]';
        break;
      case 'inTwoWeeks':
        queryFormat = '{0}:[* TO NOW/DAY+14DAYS]';
        break;
      case 'inOneMonth':
        queryFormat = '{0}:[* TO NOW/DAY+1MONTH]';
        break;
    }

    return FormatUtil.format(queryFormat, SOLR_FIELD);
  }

  override public function getDefaultState():Object {
    var state:Object = {}; // NOSONAR
    state[KEY] = 'any';
    state[DATE] = null;
    return state;
  }

}
}
