package com.coremedia.blueprint.personalization.elastic;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMAction;
import com.coremedia.blueprint.common.contentbeans.CMObject;
import com.coremedia.blueprint.common.contentbeans.CMTaxonomy;
import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.common.contentbeans.Page;
import com.coremedia.blueprint.personalization.forms.FormField;
import com.coremedia.blueprint.personalization.forms.PersonalizationForm;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.common.logging.PersonalDataLogger;
import com.coremedia.common.personaldata.PersonalData;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import com.coremedia.objectserver.view.substitution.Substitution;
import com.coremedia.personalization.context.BasicPropertyMaintainer;
import com.coremedia.personalization.context.ContextCollection;
import com.coremedia.personalization.context.PropertyProfile;
import com.coremedia.personalization.context.PropertyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("UnusedDeclaration") // service methods are called from within JSP
@Named
public class InterestsService {

  private static final Logger LOG = LoggerFactory.getLogger(InterestsService.class);
  private static final PersonalDataLogger PERSONAL_DATA_LOG = new PersonalDataLogger(LOG);

  private static final String NUMBER_OF_EXPLICIT_INTERESTS = "numberOfExplicitInterests";
  public static final String EXPLICIT_PERSONALIZATION = "explicit.personalization";
  private static final double TAXONOMY_ACCEPTED_THRESHOLD = 0.5;

  @Inject
  private ContextCollection contextCollection;

  @Inject
  private InterestsConfiguration interestsConfiguration;

  @Inject
  private ContentRepository contentRepository;

  @Inject
  private DataViewFactory dataViewFactory;

  @Inject
  private ContentBeanFactory contentBeanFactory;

  @Inject
  private SettingsService settingsService;

  /**
   * @return Provides an {@link Interests} bean based on an action
   */
  @Substitution("personalization")
  public Interests createInterestsBean(CMAction action) {
    return new Interests(action, this, contentBeanFactory, settingsService);
  }

  public @PersonalData Map<CMTaxonomy, Double> getImplicitSubjectTaxonomies() {
    return getImplicitTaxonomies(interestsConfiguration.getImplicitSubjectTaxonomyContextName());
  }

  public @PersonalData Map<CMTaxonomy, Double> getImplicitLocationTaxonomies() {
    return getImplicitTaxonomies(interestsConfiguration.getImplicitLocationTaxonomyContextName());
  }

  /**
   * Returns a list of taxonomy beans corresponding to the property names in the current user's 'explicit' interests context
   *
   * @return a list of taxonomy content beans
   */
  public List<CMTaxonomy> getExplicitUserInterests() {
    final List<CMTaxonomy> result = new ArrayList<>();
    final String explicitContextName = interestsConfiguration.getExplicitContextName();
    final @PersonalData PropertyProvider profile = contextCollection.getContext(explicitContextName, PropertyProvider.class);
    if (profile != null) {
      for (String key : profile.getPropertyNames()) {
        if (IdHelper.isContentId(key)) {
          final Content c = contentRepository.getContent(key);
          if(isTaxonomy(c)) {
            result.add(contentBeanFactory.createBeanFor(c, CMTaxonomy.class));
          } else {
            LOG.info("Could not parse taxonomy from ID {}: not a taxonomy (might originate from different repository", key);
          }
        }
      }
      //noinspection unchecked
      return dataViewFactory.loadAllCached(result, null);
    }
    return result;
  }

  /**
   * Creates a PersonalizationForm having those taxonomies checked that are present in the current 'explicit'
   * context.
   *
   * @param page the current page
   * @return a PersonalizationForm to explicitly set interests (taxonomies)
   */
  public PersonalizationForm getExplicitInterests(Page page) {
    final List<FormField> result = new ArrayList<>();
    final Map<String, Object> entries = getExplicitPersonalizationSettings(page);
    if (!entries.isEmpty()) {
      @SuppressWarnings("unchecked")
      final List<CMObject> fieldNames = (List<CMObject>) entries.get("items");
      if (!fieldNames.isEmpty()) {
        final String explicitContextName = interestsConfiguration.getExplicitContextName();
        final @PersonalData PropertyProvider profile = contextCollection.getContext(explicitContextName, PropertyProvider.class);
        for (CMObject field : fieldNames) {

          @PersonalData Number number = null;
          if (profile != null) {
            number = (Number) profile.getProperty(field.getContent().getId());
          }
          if (number == null) {
            number = 0;
          }

          final FormField formField = new FormField();
          formField.setValue(number.doubleValue() > TAXONOMY_ACCEPTED_THRESHOLD);
          formField.setBean(field);
          result.add(formField);
        }
      } else {
        if (LOG.isDebugEnabled()) {
          LOG.debug("required context '{}' is not in context collection", entries.get("context.name"));
        }
      }
    } else {
      LOG.info("content setting '{}' does not provide any taxonomies", EXPLICIT_PERSONALIZATION);
    }
    final PersonalizationForm personalizationFormToSet = new PersonalizationForm();
    personalizationFormToSet.setEntries(result);
    return personalizationFormToSet;
  }

  private Map<String, Object> getExplicitPersonalizationSettings(Page page) {
    return settingsService.settingAsMap(EXPLICIT_PERSONALIZATION, String.class, Object.class, page);
  }

  /**
   * Resets the current user's 'explicit' context to those active in the given PersonalizationForm
   */
  public Object updateExplicitInterests(CMTeasable teasable, PersonalizationForm profileForm, BindingResult validationResult) {
    final @PersonalData BasicPropertyMaintainer context = getOrCreateExplicitContext();
    // create the set of names of the 'checked' properties
    final List<FormField> formProperties = profileForm.getEntries();
    context.clear();
    // set all properties that are checked in the form
    int numberOfExplicitInterests = 0;
    for (FormField field : formProperties) {
      if (field.isValue()) {
        context.setProperty(field.getBean().getContent().getId(), 1);
        numberOfExplicitInterests++;
      }
    }
    context.setProperty(NUMBER_OF_EXPLICIT_INTERESTS, numberOfExplicitInterests);
    profileForm.setActionSuccess(true);
    return profileForm;
  }

  /**
   * Get or create the 'explicit' context
   *
   * @return the 'explicit' context as an instance of BasicPropertyMaintainer
   */
  private @PersonalData BasicPropertyMaintainer getOrCreateExplicitContext() {
    final String explicitContextName = interestsConfiguration.getExplicitContextName();
    @PersonalData BasicPropertyMaintainer context = contextCollection.getContext(explicitContextName, BasicPropertyMaintainer.class);
    if (context == null) {
      // check if context has wrong type
      final @PersonalData Object o = contextCollection.getContext(explicitContextName);
      if (o != null) {
        String msg = "Context '{}' is not of required type " + BasicPropertyMaintainer.class.getCanonicalName()
                     + ", will overwrite previous context";
        PERSONAL_DATA_LOG.debug(msg + ": {}", explicitContextName, o);
        LOG.warn(msg, explicitContextName);

      }
      context = new PropertyProfile();
      contextCollection.setContext(explicitContextName, context);
    }
    return context;
  }

  /**
   * Returns the current contents of an 'implicit interests' context as a sorted mapping from taxonomies to their scores.
   *
   * @param contextName the context to read
   * @return a map sorted by their scores
   */
  private @PersonalData Map<CMTaxonomy, Double> getImplicitTaxonomies(final String contextName) {
    final Map<CMTaxonomy, Double> result = new HashMap<>();
    final @PersonalData PropertyProvider context = contextCollection.getContext(contextName, PropertyProvider.class);
    if (context != null) {

      for (String key : context.getPropertyNames()) {
        if (IdHelper.isContentId(key)) {
          final Content content = contentRepository.getContent(key);
          if(isTaxonomy(content)) {
            ContentBean contentBean = contentBeanFactory.createBeanFor(content, ContentBean.class);

            // we might deal with ids referring to content from a different repository, therefore the id might belong to a type other than CMTaxonomy
            if (contentBean instanceof CMTaxonomy) {
              final CMTaxonomy taxonomy = (CMTaxonomy) dataViewFactory.loadCached(contentBean, null);
              @SuppressWarnings("PersonalData") // Safe to put @PersonalData value into result. It's returned as @PersonalData.
              Double value = (Double) context.getProperty(key);
              result.put(taxonomy, value);
            } else {
              LOG.warn("Got unexpected content bean {} for content of type {} (expecting {})", contentBean, content.getType(), CMTaxonomy.class);
            }
          } else {
            LOG.info("Could not parse taxonomy from ID {}: not a taxonomy (might originate from different repository", key);
          }
        }
      }
      @SuppressWarnings("PersonalData") // @PersonalData from result variable just used for sorting
      final ValueComparator bvc = new ValueComparator(result);
      final @PersonalData TreeMap<CMTaxonomy, Double> sortedMap = new TreeMap<>(bvc);
      sortedMap.putAll(result);
      return sortedMap;

    } else {
      LOG.debug("required context '{}' is not in context collection", contextName);
    }

    return result;
  }

  private static boolean isTaxonomy(Content content) {
    return content != null && content.getType().isSubtypeOf(CMTaxonomy.NAME);
  }

  private static class ValueComparator implements Comparator<CMTaxonomy>, Serializable {

    private static final long serialVersionUID = 5667734265087336585L;
    private Map<CMTaxonomy, Double> base;

    public ValueComparator(Map<CMTaxonomy, Double> base) {
      this.base = base;
    }

    @Override
    public int compare(CMTaxonomy a, CMTaxonomy b) {
      int valueCompare = Double.compare(base.get(b), base.get(a));
      if (valueCompare != 0) {
        return valueCompare;
      }
      return b.getContent().getName().compareTo(a.getContent().getName());
    }
  }

  @Override
  public String toString() {
    return "InterestsService{" +
            "interestsConfiguration=" + interestsConfiguration +
            ", contextCollection=" + contextCollection +
            '}';
  }
}
