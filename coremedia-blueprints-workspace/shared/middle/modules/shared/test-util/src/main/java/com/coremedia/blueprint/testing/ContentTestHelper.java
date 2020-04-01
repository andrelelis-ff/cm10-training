package com.coremedia.blueprint.testing;

import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;

import javax.inject.Inject;

/**
 * Helper for tests accessing content and content beans.
 * Provided by {@link ContentTestConfiguration}.
 */
public class ContentTestHelper {
  @Inject
  private ContentRepository contentRepository;

  @Inject
  private ContentBeanFactory contentBeanFactory;

  /**
   * Returns the Content with the given id.
   *
   * @param id Id of Content to get
   * @return Content
   */
  public Content getContent(String id) {
    Content content = contentRepository.getContent(id);
    if (content == null) {
      throw new IllegalArgumentException("No Content found for id " + id);
    }
    return content;
  }

  /**
   * Returns the Content with the given id.
   *
   * @param id Id of Content to get
   * @return Content
   */
  public Content getContent(int id) {
    return getContent(IdHelper.formatContentId(id));
  }

  /**
   * Returns a typed ContentBean with the given id.
   *
   * @param id Id of ContentBean to get
   * @return ContentBean
   */
  @SuppressWarnings("unchecked")
  public <T> T getContentBean(int id) {
    Content content = getContent(id);
    return (T) contentBeanFactory.createBeanFor(content, ContentBean.class);
  }

  /**
   * Returns a typed ContentBean with the given id.
   *
   * @param id Id of ContentBean to get
   * @return ContentBean
   */
  @SuppressWarnings("unchecked")
  public <T> T getContentBean(String id) {
    Content content = getContent(id);
    return (T) contentBeanFactory.createBeanFor(content, ContentBean.class);
  }
}
