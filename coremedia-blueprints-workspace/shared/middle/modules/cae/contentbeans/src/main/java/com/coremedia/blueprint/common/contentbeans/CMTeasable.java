package com.coremedia.blueprint.common.contentbeans;

import com.coremedia.blueprint.common.cta.CallToActionButtonSettings;
import com.coremedia.blueprint.common.teaser.TeaserSettings;
import com.coremedia.blueprint.common.teaserOverlay.TeaserOverlaySettings;
import com.coremedia.blueprint.common.teaserOverlay.TeaserOverlayStyle;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.xml.Markup;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * Each content has an embedded Teaser and a detailText for the page view.
 * </p>
 * <p>
 * If you need different teasers for the document, you can use additional
 * {@link CMTeaser} documents.
 * </p>
 * <p>
 * Represents the document type {@link #NAME CMTeasable}.
 * </p>
 *
 * @cm.template.api
 */
public interface CMTeasable extends CMHasContexts {
  /**
   * {@link com.coremedia.cap.content.ContentType#getName() Name of the ContentType} 'CMTeasable'.
   */
  String NAME = "CMTeasable";

  /**
   * Name of the document property 'authors'.
   */
  String AUTHORS = "authors";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a {@link CMTeasable} object
   */
  @Override
  CMTeasable getMaster();

  @Override
  Map<Locale, ? extends CMTeasable> getVariantsByLocale();

  @Override
  Collection<? extends CMTeasable> getLocalizations();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  Map<String, ? extends Aspect<? extends CMTeasable>> getAspectByName();

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  List<? extends Aspect<? extends CMTeasable>> getAspects();

  /**
   * Name of the document property 'teaserTitle'.
   */
  String TEASER_TITLE = "teaserTitle";

  /**
   * <p>
   * Returns the value of the document property {@link #TEASER_TITLE}.
   * </p>
   *
   * @return the value of the document property {@link #TEASER_TITLE}
   * @cm.template.api
   */
  String getTeaserTitle();

  /**
   * Name of the document property 'teaserText'.
   */
  String TEASER_TEXT = "teaserText";

  /**
   * Returns the value of the document property {@link #TEASER_TEXT}.
   *
   * @return the value of the document property {@link #TEASER_TEXT}
   * @cm.template.api
   */
  Markup getTeaserText();

  /**
   * Name of the document property 'detailText'.
   */
  String DETAIL_TEXT = "detailText";

  /**
   * Returns the value of the document property {@link #DETAIL_TEXT}.
   *
   * @return the value of the document property {@link #DETAIL_TEXT}
   * @cm.template.api
   */
  Markup getDetailText();

  /**
   * Name of the document property 'pictures'.
   * <p>
   * Meanwhile the type of the pictures linklist has been widened to CMMedia,
   * but we kept the name "pictures" for compatibility reasons.
   */
  String PICTURES = "pictures";

  /**
   * Returns the pictures of the Teasable.
   *
   * @return the CMPicture subset of {@link #getMedia()}
   * @cm.template.api
   */
  List<CMPicture> getPictures();

  /**
   * Returns the media of the Teasable.
   *
   * @cm.template.api
   */
  @NonNull
  List<CMMedia> getMedia();

  /**
   * Functionally equivalent to {@link #getMedia()}
   * <p>
   * If your contentbean delegates to other contentbeans in order to accumulate
   * more media objects, it should support this method in order to prevent
   * infinite recursions.
   */
  @NonNull
  default List<CMMedia> fetchMediaWithRecursionDetection(Collection<CMTeasable> visited) {
    return getMedia();
  }

  /**
   * Returns the first CMPicture stored in the document property {@link #PICTURES}.
   *
   * @return the first CMPicture stored in the document property {@link #PICTURES}
   * @cm.template.api
   */
  CMPicture getPicture();

  /**
   * Returns the first element of document property {@link #PICTURES}.
   *
   * @return the first element of the document property {@link #PICTURES}
   * @cm.template.api
   */
  CMMedia getFirstMedia();

  /**
   * Returns this. Overridden by standalone teasers.
   *
   * @return a {@link CMLinkable} object
   * @cm.template.api
   */
  CMLinkable getTarget();

  /**
   * Name of the document property 'searchable'.
   */
  String NOT_SEARCHABLE = "notSearchable";

  /**
   * Returns the value of the document property {@link #NOT_SEARCHABLE}.
   *
   * @return the value of the document property {@link #NOT_SEARCHABLE}
   */
  boolean isNotSearchable();

  /**
   * Name of the document property 'related'.
   */
  String RELATED = "related";

  /**
   * Returns the value of the document property {@link #RELATED}.
   *
   * @return a list of {@link CMTeasable} objects
   * @cm.template.api
   */
  List<? extends CMTeasable> getRelated();

  /**
   * Returns the {@link CMTeasable}s referring to this {@link CMTeasable} in their {@link #RELATED} property.
   *
   * @return a list of {@link CMTeasable} objects
   */
  List<? extends CMTeasable> getRelatedByReferrers();

  /**
   * Returns {@link CMTeasable}s that are related to this item because they have a similar set of taxonomies
   * linked.
   *
   * @return a list of {@link CMTeasable} objects
   */
  List<? extends CMTeasable> getRelatedBySimilarTaxonomies();

  /**
   * Returns a list of related {@link CMTeasable}s which is a merge of {@link CMTeasable#getRelated()},
   * {@link CMTeasable#getRelatedByReferrers()}, {@link CMTeasable#getRelatedBySimilarTaxonomies()}.
   *
   * @return a list of {@link CMTeasable} objects
   */
  List<? extends CMTeasable> getRelatedAll();

  /**
   * Returns a list of related {@link CMTeasable}s which is a merge of {@link CMTeasable#getRelatedByReferrers()},
   * {@link CMTeasable#getRelatedBySimilarTaxonomies()}.
   *
   * @return a list of {@link CMTeasable} objects
   * @cm.template.api
   */
  List<? extends CMTeasable> getRelatedImplicitly();

  /**
   * Returns a map where the keys are document type names and the values are Lists of {@link CMTeasable}s dynamically
   * related to this object.
   *
   * @return a Map of String to Lists of CMTeasable
   * @cm.template.api
   * @see #getRelatedAll()
   */
  Map<String, List<CMTeasable>> getRelatedAllByType();

  /**
   * Returns a map where the keys are document type names and the values are Lists of {@link CMTeasable}s explicitly
   * related to this object.
   *
   * @return a Map of String to Lists of CMTeasable
   * @see #getRelatedImplicitly()
   */
  Map<String, List<CMTeasable>> getRelatedImplicitlyByType();

  /**
   * Returns the detail text splitted at each paragraph.
   *
   * @return the detail text splitted at each paragraph.the detail text splitted at each paragraph.
   * @cm.template.api
   */
  List<Markup> getTextAsParagraphs();

  /**
   * The name of the teaser options settings struct.
   */
  String TEASER_SETTINGS_STRUCT_NAME = "teaserSettings";

  /**
   * Returns the teaser settings.
   *
   * @return the teaser settings.
   * @cm.template.api
   */
  TeaserSettings getTeaserSettings();

  /**
   * The name of the teaser overlay settings struct.
   */
  String TEASER_OVERLAY_SETTINGS_STRUCT_NAME = "teaserOverlay";

  /**
   * The name of the style settings in the teaser overlay settings sub struct.
   */
  String TEASER_OVERLAY_SETTINGS_STYLE_SUB_STRUCT_NAME = "style";

  /**
   * Returns the settings for the teaser overlay feature.
   *
   * @return the settings for the teaser overlay feature.
   * @cm.template.api
   */
  TeaserOverlaySettings getTeaserOverlaySettings();

  /**
   * Returns the style for the teaser overlay feature.
   *
   * @return the style for the teaser overlay feature.
   * @cm.template.api
   */
  TeaserOverlayStyle getTeaserOverlayStyle();

  /**
   * Returns the settings for all Call-To-Action buttons.
   *
   * @return the settings for all Call-To-Action buttons.
   * @cm.template.api
   */
  List<CallToActionButtonSettings> getCallToActionSettings();

  /**
   * <p>
   * Returns the value of the document property {@link #AUTHORS}.
   * </p>
   *
   * @return the value of the document property {@link #AUTHORS}
   * @cm.template.api
   */
  List<CMPerson> getAuthors();
}
