package com.coremedia.blueprint.personalization.elastic;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.action.CMActionState;
import com.coremedia.blueprint.common.contentbeans.CMAction;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.common.personaldata.PersonalData;
import com.coremedia.objectserver.beans.ContentBeanFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A bean representing the interests of the current user
 */
public class Interests implements CMActionState {

  private final CMAction action;
  private final InterestsService service;
  private final ContentBeanFactory contentBeanFactory;
  private final SettingsService settingsService;

  public Interests(CMAction action, InterestsService service, ContentBeanFactory contentBeanFactory, SettingsService settingsService) {
    this.action = action;
    this.service = service;
    this.contentBeanFactory = contentBeanFactory;
    this.settingsService = settingsService;
  }

  @Override
  public CMAction getAction() {
    return action;
  }


  public @PersonalData Map<CMTaxonomy, Double> getImplicitSubjectTaxonomies() {
    if( isEnabled("show.implicit.subject.taxonomies")) {
      return service.getImplicitSubjectTaxonomies();
    }
    else {
      return Collections.emptyMap();
    }
  }

  public @PersonalData Map<CMTaxonomy, Double> getImplicitLocationTaxonomies() {
    if( isEnabled("show.implicit.location.taxonomies")) {
      return service.getImplicitLocationTaxonomies();
    }
    else {
      return Collections.emptyMap();
    }
  }

  public List<CMTaxonomy> getExplicitUserInterests() {
    return service.getExplicitUserInterests();
  }

  // ==========================

  private boolean isEnabled(String key) {
    CMAction cmAction = contentBeanFactory.createBeanFor(action.getContent(), CMAction.class);
    Map<String, Object> personalizationSettings = settingsService.settingAsMap("personalization", String.class, Object.class, cmAction);
    return Boolean.TRUE.equals(personalizationSettings.get(key));
  }

  @Override
  public String toString() {
    return "Interests{" +
            "action=" + action +
            ", service=" + service +
            '}';
  }
}
