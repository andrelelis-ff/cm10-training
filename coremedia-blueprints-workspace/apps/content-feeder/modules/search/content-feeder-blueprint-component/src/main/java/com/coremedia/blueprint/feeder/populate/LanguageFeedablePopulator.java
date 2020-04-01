package com.coremedia.blueprint.feeder.populate;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.feeder.MutableFeedable;
import com.coremedia.cap.feeder.TextParameters;
import com.coremedia.cap.feeder.populate.FeedablePopulator;
import com.coremedia.cap.multisite.SitesService;
import org.springframework.beans.factory.annotation.Required;

import java.util.Locale;

public class LanguageFeedablePopulator implements FeedablePopulator<Content> {
  static final String SOLR_LANGUAGE_FIELD_NAME = "language";

  private SitesService sitesService;

  @Override
  public void populate(MutableFeedable feedable, Content content) {
    if (feedable == null || content == null) {
      throw new IllegalArgumentException("mutableFeedable and content must not be null");
    }

    if (content.getType().isSubtypeOf("CMLocalized")) {
      Locale locale = sitesService.getContentSiteAspect(content).getLocale();
      String language = locale.getLanguage();
      if (!language.isEmpty()) {
        feedable.setStringElement(SOLR_LANGUAGE_FIELD_NAME, language, TextParameters.NONE.asMap());
      }
    }
  }

  @Required
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }
}
