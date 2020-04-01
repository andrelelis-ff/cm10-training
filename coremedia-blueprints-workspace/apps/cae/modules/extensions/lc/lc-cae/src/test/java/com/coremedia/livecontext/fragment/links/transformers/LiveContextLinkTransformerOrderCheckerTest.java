package com.coremedia.livecontext.fragment.links.transformers;

import com.coremedia.objectserver.web.links.LinkTransformer;
import org.junit.Test;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;

import static java.util.Collections.singletonList;

public class LiveContextLinkTransformerOrderCheckerTest {

  @Test(expected = IllegalStateException.class)
  public void noLiveContextLinkTransformer() {
    GenericApplicationContext genericApplicationContext = new GenericApplicationContext();
    genericApplicationContext.registerBean("linkTransformers", List.class,
            () -> singletonList((LinkTransformer) (s, o, s1, httpServletRequest, httpServletResponse, b) -> null)
    );
    genericApplicationContext.registerBean(ApplicationListener.class,
            () -> (ApplicationListener<ContextRefreshedEvent>) LiveContextLinkTransformerOrderChecker::validateOrder);
    genericApplicationContext.refresh();
  }

  @Test
  public void liveContextLinkTransformer() {
    GenericApplicationContext genericApplicationContext = new GenericApplicationContext();
    genericApplicationContext.registerBean("linkTransformers", List.class,
            () -> singletonList(new LiveContextLinkTransformer())
    );
    genericApplicationContext.registerBean(ApplicationListener.class,
            () -> (ApplicationListener<ContextRefreshedEvent>) LiveContextLinkTransformerOrderChecker::validateOrder);
    genericApplicationContext.refresh();
  }

}