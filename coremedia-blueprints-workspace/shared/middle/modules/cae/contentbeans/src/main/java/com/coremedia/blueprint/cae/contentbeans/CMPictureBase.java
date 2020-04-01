package com.coremedia.blueprint.cae.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMPicture;
import com.coremedia.cae.aspect.Aspect;
import com.coremedia.cap.common.Blob;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generated base class for immutable beans of document type CMPicture.
 * Should not be changed.
 */
public abstract class CMPictureBase extends CMVisualImpl implements CMPicture {

  private static final String DISABLE_CROPPING = "disableCropping";

  private static final String FOCUS_AREA = "focusArea";
  private static final String FOCUS_POINT = "focusPoint";

  private static final Point2D.Double DEFAULT_FOCUS_POINT = new Point2D.Double(0.5, 0.5);
  private static final Rectangle2D.Double DEFAULT_FOCUS_AREA = new Rectangle2D.Double(0.0, 0.0, 1.0, 1.0);


  /**
   * Returns the value of the document property {@link #MASTER}.
   *
   * @return a list of {@link com.coremedia.blueprint.common.contentbeans.CMPicture} objects
   */
  @Override
  public CMPicture getMaster() {
    return (CMPicture) super.getMaster();
  }

  @Override
  public Map<Locale, ? extends CMPicture> getVariantsByLocale() {
    return getVariantsByLocale(CMPicture.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<? extends CMPicture> getLocalizations() {
    return (Collection<? extends CMPicture>) super.getLocalizations();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ? extends Aspect<? extends CMPicture>> getAspectByName() {
    return (Map<String, ? extends Aspect<? extends CMPicture>>) super.getAspectByName();
  }

  /**
   * @deprecated since 1907.1; Implement optional features as extensions.
   */
  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<? extends Aspect<? extends CMPicture>> getAspects() {
    return (List<? extends Aspect<? extends CMPicture>>) super.getAspects();
  }

  @Override
  public Blob getData() {
    return getContent().getBlobRef(DATA);
  }

  @Override
  public boolean getDisableCropping() {
    Boolean disableCroppingBoolean = getSettingsService().setting(DISABLE_CROPPING, Boolean.class, getContent());
    if (disableCroppingBoolean == null) {
      Integer disableCroppingInteger = getSettingsService().setting(DISABLE_CROPPING, Integer.class, getContent());
      return disableCroppingInteger != null && disableCroppingInteger != 0;
    } else {
      return disableCroppingBoolean;
    }
  }

  @Override
  public Point2D getFocusPoint() {
    Map<String, String> focusPointMap = getSettingsService().settingAsMap(FOCUS_POINT, String.class, String.class, this);

    Point2D.Double focusPointSetting = new Point2D.Double(
            Double.parseDouble(focusPointMap.getOrDefault("x", String.valueOf(DEFAULT_FOCUS_POINT.getX()))),
            Double.parseDouble(focusPointMap.getOrDefault("y", String.valueOf(DEFAULT_FOCUS_POINT.getY())))
    );

    if (!focusPointSetting.equals(DEFAULT_FOCUS_POINT)) {
      // If the focus point setting does not equal the default, it was set
      // explicitly and we can directly return it.
      return focusPointSetting;
    } else {
      // If the focus point setting equals the default, it needs to be computed
      // relatively to the focus area.
      Map<String, String> focusAreaMap = getSettingsService().settingAsMap(FOCUS_AREA, String.class, String.class, this);
      Rectangle2D.Double focusAreaAnchorSetting = new Rectangle2D.Double(
              Double.parseDouble(focusAreaMap.getOrDefault("x1", String.valueOf(DEFAULT_FOCUS_AREA.getX()))),
              Double.parseDouble(focusAreaMap.getOrDefault("y1", String.valueOf(DEFAULT_FOCUS_AREA.getY()))),
              Double.parseDouble(focusAreaMap.getOrDefault("x2", String.valueOf(DEFAULT_FOCUS_AREA.getWidth()))),
              Double.parseDouble(focusAreaMap.getOrDefault("y2", String.valueOf(DEFAULT_FOCUS_AREA.getHeight())))
      );

      return new Point2D.Double(
              focusAreaAnchorSetting.getX() + focusAreaAnchorSetting.getWidth() / 2,
              focusAreaAnchorSetting.getY() + focusAreaAnchorSetting.getHeight() / 2
      );
    }
  }
}
  
