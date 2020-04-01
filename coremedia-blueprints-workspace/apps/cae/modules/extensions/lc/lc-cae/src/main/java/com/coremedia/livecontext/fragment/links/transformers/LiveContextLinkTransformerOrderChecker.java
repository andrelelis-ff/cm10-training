package com.coremedia.livecontext.fragment.links.transformers;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Streams.findLast;

/**
 * Checks if the {@link com.coremedia.livecontext.fragment.links.transformers.resolvers.LiveContextLinkResolver}
 * is the last element in the 'linkTransformers' list bean.
 */
class LiveContextLinkTransformerOrderChecker {

  private static final String LINK_TRANSFORMERS = "linkTransformers";

  private LiveContextLinkTransformerOrderChecker() {
  }

  static void validateOrder(ContextRefreshedEvent contextRefreshedEvent) {
    ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
    List<?> linkTransformers = applicationContext.getBean(LINK_TRANSFORMERS, List.class);

    // check if the LiveContextLinkResolver is the last in the linkTransformers list
    Optional<?> linkTransformer = findLast(linkTransformers.stream())
            .filter(LiveContextLinkTransformer.class::isInstance);

    if (!linkTransformer.isPresent()) {
      throw new IllegalStateException("Last link transformer not of type " + LiveContextLinkTransformer.class + ": " + linkTransformers);
    }
  }

}
