package com.coremedia.blueprint.analytics.elastic.contentbeans;

import com.coremedia.blueprint.common.contentbeans.CMCollection;
import com.coremedia.blueprint.common.contentbeans.CMPicture;
import com.coremedia.cap.content.Content;
import com.coremedia.objectserver.beans.ContentBean;
import com.google.common.collect.Iterables;

import java.util.List;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.ImmutableList.copyOf;

/**
 *  Implementation class for beans of document type "CMALXEventList".
 */
public class CMALXEventListImpl extends CMALXEventListBase {

  /**
   * @return The unmodified tracked events, which are custom Strings (depending on what you tracked)
   */
  @Override
  @SuppressWarnings("unchecked")
  public List<CMPicture> getItemsUnfiltered() {
    int maxLength = getMaxLength();

    final Iterable filteredPictures = Iterables.filter(getTrackedItemsUnfiltered(), instanceOf(CMPicture.class));

    // although CMALXEventLists are designed to work on arbitrary events (producing arbitrary objects for rendering)
    // we only render pictures for now - just move filtering somewhere else if this is to be refactored

    List<CMPicture> result = copyOf(filteredPictures).subList(0,  Math.min(Iterables.size(filteredPictures), maxLength));

    // default content
    if (result.isEmpty()) {
      List<Content> defaultContentLinks = getContent().getLinks(CMCollection.ITEMS);
      List defaultContentBeans = createBeansFor(defaultContentLinks, ContentBean.class);
      final Iterable filteredDefaultContent = Iterables.filter(defaultContentBeans, instanceOf(CMPicture.class));
      result = copyOf(filteredDefaultContent).subList(0, Math.min(Iterables.size(filteredDefaultContent), maxLength));
    }
    return result;
  }
}
