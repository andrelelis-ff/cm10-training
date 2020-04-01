package com.coremedia.blueprint.common.navigation.context.selector;

import com.coremedia.blueprint.base.navigation.context.selector.ContextSelector;
import com.coremedia.blueprint.common.contentbeans.CMContext;
import com.coremedia.blueprint.common.contentbeans.CMObject;
import com.coremedia.cap.content.Content;
import com.coremedia.objectserver.beans.ContentBean;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.dataviews.DataViewFactory;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A ContextSelector on ContentBean layer
 * <p>
 * Only responsible for contentbean and dataview wrapping,
 * the actual selection is done by a UAPI based delegate.
 */
public class LinkableBeanContextSelector implements ContextSelector<CMContext, CMObject> {

  private final ContextSelector<Content, Content> delegate;
  private final ContentBeanFactory contentBeanFactory;
  private final DataViewFactory dataViewFactory;

  public LinkableBeanContextSelector(ContextSelector<Content, Content> cs, ContentBeanFactory contentBeanFactory, DataViewFactory dataViewFactory) {
    this.delegate = cs;
    this.contentBeanFactory = contentBeanFactory;
    this.dataViewFactory = dataViewFactory;
  }

  @Nullable
  @Override
  public CMContext selectContext(@Nullable CMContext currentContext, @NonNull List<? extends CMContext> candidates, @Nullable CMObject target) {
    Content currentContextContent = currentContext != null ? currentContext.getContent() : null;
    Content targetContent = target != null ? target.getContent() : null;
    List<Content> candidateContents = candidates.stream().map(ContentBean::getContent).collect(Collectors.toList());
    Content context = delegate.selectContext(currentContextContent, candidateContents, targetContent);
    return context != null ? dataViewFactory.loadCached(contentBeanFactory.createBeanFor(context, CMContext.class), null) : null;
  }

}
