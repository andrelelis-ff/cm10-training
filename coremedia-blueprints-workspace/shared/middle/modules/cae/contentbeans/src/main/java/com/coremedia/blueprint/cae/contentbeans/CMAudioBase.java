package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMAudio;
import com.coremedia.blueprint.common.player.PlayerSettings;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.common.Blob;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMAudio.
 * Should not be changed.
 */
public abstract class CMAudioBase extends CMMediaImpl implements CMAudio {

  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMAudio} objects
   */
  @Override
  public CMAudio getMaster() {
    return (CMAudio) super.getMaster();
  }


  @Override
  public Map<Locale, ? extends CMAudio> getVariantsByLocale() {
    return getVariantsByLocale(CMAudio.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMAudio> getLocalizations() {
    return (Collection<? extends CMAudio>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMAudio>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMAudio>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMAudio>> getAspects() {
    return (List<? extends Aspect<? extends CMAudio>>) super.getAspects();
  }

  @Override
  public Blob getData() {
    return getContent().getBlobRef(DATA);
  }

  @Override
  public String getDataUrl() {
    return getContent().getString(DATA_URL);
  }

  @Override
  public PlayerSettings getPlayerSettings() {
    Map<String, Object> mapping = getSettingsService().settingAsMap(CMAudio.PLAYER_SETTINGS, String.class, Object.class, this);

    return getSettingsService().createProxy(PlayerSettings.class, mapping);
  }
}
