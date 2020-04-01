package com.coremedia.livecontext.fragment.links.transformers;

import com.coremedia.livecontext.fragment.FragmentContext;
import com.coremedia.livecontext.fragment.FragmentContextProvider;
import com.coremedia.objectserver.web.links.LinkTransformer;
import com.coremedia.objectserver.web.links.ParameterAppendingLinkTransformer;
import edu.umd.cs.findbugs.annotations.NonNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static com.coremedia.livecontext.fragment.FragmentContextProvider.FRAGMENT_CONTEXT_PARAMETER;

/**
 * In the context of a fragment request and when a dynamic include url is built
 * this transformer adds the current fragment context as request parameter.
 * This context can then be recreated when the dynamic include request is being processed.
 */
public class FragmentParamsAppendingLinkTransformer implements LinkTransformer {

  private final ParameterAppendingLinkTransformer parameterAppender;

  public FragmentParamsAppendingLinkTransformer() {
    parameterAppender = new ParameterAppendingLinkTransformer();
    parameterAppender.setParameterName(FRAGMENT_CONTEXT_PARAMETER);
  }

  @Override
  public String transform(String source, Object bean, String view, @NonNull HttpServletRequest request,
                          @NonNull HttpServletResponse response, boolean forRedirect) {
    Optional<FragmentContext> fragmentContext = FragmentContextProvider.findFragmentContext(request);
    if (fragmentContext.isPresent() && fragmentContext.get().isFragmentRequest() && isDynamicFragment(source)) {
      String fragmentParamsValue = fragmentContext.get().getParameters().toQueryParam();
      if (!fragmentParamsValue.isEmpty()) {
        request.setAttribute(FRAGMENT_CONTEXT_PARAMETER, fragmentParamsValue);
        return parameterAppender.transform(source, bean, view, request, response, forRedirect);
      }
    }

    return source;
  }

  private boolean isDynamicFragment(String source) {
    return source != null && (source.contains("/dynamic/fragment/") || source.contains("/dynamic/placement/"));
  }
}
