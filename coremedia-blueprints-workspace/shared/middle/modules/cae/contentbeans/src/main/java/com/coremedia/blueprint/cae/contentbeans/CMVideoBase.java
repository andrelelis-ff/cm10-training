package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMTeasable;
import com.coremedia.blueprint.common.contentbeans.CMVideo;
import com.coremedia.blueprint.common.player.PlayerSettings;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMVideo.
 * Should not be changed.
 */
public abstract class CMVideoBase extends CMVisualImpl implements CMVideo {

  String DEFAULTTARGET = "defaultTarget";
  String SEQUENCES = "sequences";

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMVideo} objects
   */
  @Override
  public CMVideo getMaster() {
    return (CMVideo) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMVideo> getVariantsByLocale() {
    return getVariantsByLocale(CMVideo.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMVideo> getLocalizations() {
    return (Collection<? extends CMVideo>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMVideo>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMVideo>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMVideo>> getAspects() {
    return (List<? extends Aspect<? extends CMVideo>>) super.getAspects();
  }

  @Override
  public Blob getData() {
    return getContent().getBlobRef(DATA);
  }

  /* --- for shoppable videos --- */

  /**
   * Returns the struct object for the Struct XML property 'timeLine'.
   */
  public Struct getTimeLine() {
    return getContent().getStruct(TIMELINE);
  }

  /**
   * Returns the value of the 'defaultTarget' link property that is defined inside the 'timeLine' struct.
   */
  public CMTeasable getTimeLineDefaultTarget() {
    Struct timeLine = getTimeLine();
    if(timeLine != null && timeLine.getType().getDescriptor(DEFAULTTARGET) != null) {
      Content defaultTarget = timeLine.getLink(DEFAULTTARGET);
      return defaultTarget == null ? null : getContentBeanFactory().createBeanFor(defaultTarget, CMTeasable.class);
    }
    return null;
  }

  /**
   * The timeline struct contains a list of structs.
   * Each struct contains the start time in milliseconds and the linked target.
   * The converted struct is returned here as a list of mapped values where Content has been converted to ContentBeans.
   */
  public List getTimeLineSequences() {
    List<Map<String,Object>> sequenceList = new ArrayList<>();
    Struct timeLine = getTimeLine();
    if(timeLine != null && timeLine.getType().getDescriptor(SEQUENCES) != null) {
      List<Struct> sequences = timeLine.getStructs(SEQUENCES);
      for (Struct sequence : sequences) {
        Map<String,Object> sequenceMap = getContentBeanFactory().createBeanMapFor(sequence);
        sequenceList.add(sequenceMap);
      }
    }
    return sequenceList;
  }

  @Override
  public PlayerSettings getPlayerSettings() {
    Map<String, Object> mapping = getSettingsService().settingAsMap(CMVideo.PLAYER_SETTINGS, String.class, Object.class, this);

    return getSettingsService().createProxy(PlayerSettings.class, mapping);
  }
}
