package com.coremedia.blueprint.personalization.preview;

import com.coremedia.blueprint.cae.layout.ContentBeanBackedPageGridPlacement;
import com.coremedia.blueprint.common.contentbeans.CMExternalLink;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.layout.DynamizableContainer;
import com.coremedia.objectserver.web.links.LinkTransformer;
import com.coremedia.objectserver.web.links.ParameterAppendingLinkTransformer;
import com.coremedia.personalization.preview.PreviewPersonalizationHandlerInterceptor;
import com.coremedia.personalization.preview.TestContextSource;
import edu.umd.cs.findbugs.annotations.NonNull;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * LinkTransformer implementation that adds the request parameter for the test user context to page links.
 * This ensures that navigating in the preview within the editor does
 * not loose the selected test context after clicking a hyperlink.
 */
public class PersonaParameterAppendingLinkTransformer implements LinkTransformer {

  private final ParameterAppendingLinkTransformer testContextAppender;
  private final ParameterAppendingLinkTransformer testContextIdAppender;

  public PersonaParameterAppendingLinkTransformer() {
    testContextAppender = new ParameterAppendingLinkTransformer();
    testContextAppender.setParameterName(PreviewPersonalizationHandlerInterceptor.QUERY_PARAMETER_TESTCONTEXT);
    testContextIdAppender = new ParameterAppendingLinkTransformer();
    testContextIdAppender.setParameterName(TestContextSource.QUERY_PARAMETER_TESTCONTEXTID);
  }

  @PostConstruct void initialize() throws Exception {
    testContextAppender.afterPropertiesSet();
    testContextIdAppender.afterPropertiesSet();
  }

  @Override
  public String transform(String source, Object bean, String view, @NonNull HttpServletRequest request,
                          @NonNull HttpServletResponse response, boolean forRedirect) {
    if((bean instanceof CMLinkable || bean instanceof ContentBeanBackedPageGridPlacement
            || bean instanceof DynamizableContainer) && !(bean instanceof CMExternalLink)) {
      String transformed = testContextAppender.transform(source, bean, view, request, response, forRedirect);
      return testContextIdAppender.transform(transformed, bean, view, request, response, forRedirect);
    }
    return source;
  }

}
