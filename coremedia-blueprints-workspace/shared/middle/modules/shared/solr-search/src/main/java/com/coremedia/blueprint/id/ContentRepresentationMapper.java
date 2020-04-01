package com.coremedia.blueprint.id;

import com.coremedia.cap.content.Content;
import com.coremedia.objectserver.beans.ContentBean;

public class ContentRepresentationMapper extends RepresentationMapper<Object> {

  @Override
  public boolean isValid(Object bean) {
    if (!(super.isValid(bean))) {
      return false;
    }

    Content content = null;
    if (bean instanceof ContentBean) {
      content = ((ContentBean) bean).getContent();
    } else if (bean instanceof Content) {
      content = (Content) bean;
    }
    return (content != null && content.isInProduction());
  }
}