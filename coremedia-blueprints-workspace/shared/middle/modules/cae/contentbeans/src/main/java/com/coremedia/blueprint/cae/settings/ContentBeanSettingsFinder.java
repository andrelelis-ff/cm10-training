package com.coremedia.blueprint.cae.settings;

import com.coremedia.blueprint.base.settings.SettingsFinder;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.dataviews.DataViewCollections;
import com.coremedia.objectserver.dataviews.DataViewHelper;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.List;

/**
 * Supports settings access on contentbeans and dataviews.
 * <p>
 * Delegates down to UAPI level settings access and wraps the results back
 * into the layer you came from.
 * <p>
 * The actual settings lookup depends on the SettingsFinders configured for
 * content.  By Blueprint default, localSettings and linkedSettings are
 * supported for any document type, esp. for CMLinkable, which this concept
 * originates from.
 */
public class ContentBeanSettingsFinder implements SettingsFinder {
  private ContentBeanFactory contentBeanFactory;
  private DataViewCollections dataViewCollections;


  // --- construct and configure ------------------------------------

  @Required
  public void setContentBeanFactory(ContentBeanFactory contentBeanFactory) {
    this.contentBeanFactory = contentBeanFactory;
  }

  @Required
  public void setDataViewCollections(DataViewCollections dataViewCollections) {
    this.dataViewCollections = dataViewCollections;
  }


  // --- SettingsFinder ---------------------------------------------

  @Override
  public Object setting(Object bean, String name, SettingsService settingsService) {
    if (!(bean instanceof ContentBean)) {
      return null;
    }
    // Delegate down to UAPI level
    Object setting = settingsService.setting(name, Object.class, ((ContentBean)bean).getContent());
    // Back to beans
    Object contentBeanedResult = toContentBeans(setting);
    // If the source bean is a dataview, return a dataviewed result.
    return DataViewHelper.isDataView(bean) ? dataViewCollections.nestedDataview(contentBeanedResult) : contentBeanedResult;
  }


  // --- internal ---------------------------------------------------

  /**
   * Cast any Content results back into the ContentBean domain.
   */
  private Object toContentBeans(Object value) {
    if (value instanceof Content) {
      return contentBeanFactory.createBeanFor((Content) value, ContentBean.class);
    }
    if (value instanceof Struct) {
      return contentBeanFactory.createBeanMapFor((Struct)value);
    }
    if (value instanceof List) {
      List list = (List)value;
      ArrayList<Object> result = new ArrayList<>(list.size());
      for (Object item : list) {
        result.add(toContentBeans(item));
      }
      return result;
    }
    return value;
  }
}
