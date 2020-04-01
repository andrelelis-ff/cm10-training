package com.coremedia.blueprint.osm;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMLocTaxonomy;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

/**
 * @deprecated since 1907.1; Currently unused. If needed again, reimplement it as extension.
 */
@Deprecated
public class OpenStreetMapAspectImpl implements OpenStreetMapAspect {
  private CMTeasable aggregator;
  private SettingsService settingsService;

  @Override
  public boolean isEnabled() {
    boolean isEnabled = settingsService.settingWithDefault("detail.show.map", Boolean.class, !aggregator.getLocationTaxonomy().isEmpty(), aggregator)
            && !aggregator.getLocationTaxonomy().isEmpty();
    if (isEnabled) {
      CMLocTaxonomy taxonomy = aggregator.getLocationTaxonomy().get(0);
      return StringUtils.isNotBlank(taxonomy.getLatitude()) && StringUtils.isNotBlank(taxonomy.getLongitude());
    }
    return isEnabled;
  }

  @Override
  public void setAspectAggregator(Object aggregator) {
    this.aggregator = (CMTeasable) aggregator;
  }

  @Override
  public CMTeasable getAggregator() {
    return aggregator;
  }

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }
}
