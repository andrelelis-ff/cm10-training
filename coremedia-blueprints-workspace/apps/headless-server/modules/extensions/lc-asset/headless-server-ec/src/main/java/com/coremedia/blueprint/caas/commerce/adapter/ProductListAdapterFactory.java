package com.coremedia.blueprint.caas.commerce.adapter;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.caas.commerce.model.CommerceFacade;
import com.coremedia.caas.model.adapter.ExtendedLinkListAdapterFactory;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.SitesService;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Objects;

@DefaultAnnotation(NonNull.class)
public class ProductListAdapterFactory {
  private SettingsService settingsService;
  private final SitesService sitesService;
  private ExtendedLinkListAdapterFactory extendedLinkListAdapterFactory;
  private CommerceFacade commerceFacade;

  public ProductListAdapterFactory(SettingsService settingsService, SitesService sitesService,  ExtendedLinkListAdapterFactory extendedLinkListAdapterFactory, CommerceFacade commerceFacade) {
    this.settingsService = settingsService;
    this.sitesService = sitesService;
    this.extendedLinkListAdapterFactory = extendedLinkListAdapterFactory;
    this.commerceFacade = commerceFacade;
  }

  public ProductListAdapter to(Content queryList) {
    return new ProductListAdapter(extendedLinkListAdapterFactory, queryList, settingsService, commerceFacade,
            Objects.requireNonNull(sitesService.getContentSiteAspect(queryList).getSite()).getId());
  }
}
