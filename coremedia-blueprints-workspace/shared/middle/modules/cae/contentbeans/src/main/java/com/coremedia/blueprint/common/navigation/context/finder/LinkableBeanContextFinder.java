package com.coremedia.blueprint.common.navigation.context.finder;

import com.coremedia.blueprint.base.navigation.context.finder.ContextFinder;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.navigation.Linkable;
import com.coremedia.blueprint.common.navigation.Navigation;
import com.coremedia.cap.content.Content;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collections;
import java.util.List;

/**
 * A {@link com.coremedia.blueprint.base.navigation.context.finder.ContextFinder} that uses a configured {@link com.coremedia.blueprint.base.navigation.context.finder.ContextFinder}&lt;{@link Content},{@link Content}&gt;
 * delegate to retrieve contexts and generates content beans for those. 
 */
public class LinkableBeanContextFinder implements ContextFinder<Linkable, Navigation> {

  private ContextFinder<Content, Content> delegate;
  private ContentBeanFactory contentBeanFactory;
  private DataViewFactory dataViewFactory;

  @Override
  public List<Navigation> findContextsFor(Linkable linkable, Navigation currentContext) {
    if (!(linkable instanceof CMLinkable) || currentContext != null && !(currentContext instanceof CMContext)) {
      return Collections.emptyList();
    }
    Content contextContent = currentContext==null ? null : ((CMContext)currentContext).getContent();
    List<Content> contents = delegate.findContextsFor(((CMLinkable)linkable).getContent(), contextContent);
    List<Navigation> contentBeans = contentBeanFactory.createBeansFor(contents, Navigation.class);
    return dataViewFactory.loadAllCached(contentBeans, null);
  }

  @Override
  public List<Navigation> findContextsFor(Linkable linkable) {
    return findContextsFor(linkable, null);
  }

  @Required
  public void setDelegate(ContextFinder<Content, Content> delegate) {
    this.delegate = delegate;
  }

  @Required
  public void setContentBeanFactory(ContentBeanFactory contentBeanFactory) {
    this.contentBeanFactory = contentBeanFactory;
  }

  @Required
  public void setDataViewFactory(DataViewFactory dataViewFactory) {
    this.dataViewFactory = dataViewFactory;
  }
}
