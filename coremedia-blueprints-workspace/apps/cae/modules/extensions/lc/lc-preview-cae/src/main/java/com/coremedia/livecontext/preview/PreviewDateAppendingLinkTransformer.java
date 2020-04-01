package com.coremedia.livecontext.preview;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentStoreContext;
import com.coremedia.blueprint.common.preview.PreviewDateFormatter;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.objectserver.web.links.LinkTransformer;
import com.coremedia.objectserver.web.links.ParameterAppendingLinkTransformer;
import edu.umd.cs.findbugs.annotations.NonNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZonedDateTime;
import java.util.Optional;

import static com.coremedia.blueprint.common.datevalidation.ValidityPeriodValidator.REQUEST_PARAMETER_PREVIEW_DATE;

public class PreviewDateAppendingLinkTransformer implements LinkTransformer {

  private final ParameterAppendingLinkTransformer parameterAppender;

  public PreviewDateAppendingLinkTransformer() {
    parameterAppender = new ParameterAppendingLinkTransformer();
    parameterAppender.setParameterName(REQUEST_PARAMETER_PREVIEW_DATE);
  }

  @Override
  public String transform(String source, Object bean, String view, @NonNull HttpServletRequest request,
                          @NonNull HttpServletResponse response, boolean forRedirect) {
    findPreviewDate()
            .map(PreviewDateFormatter::format)
            .ifPresent(previewDateText -> request.setAttribute(REQUEST_PARAMETER_PREVIEW_DATE, previewDateText));

    return parameterAppender.transform(source, bean, view, request, response, forRedirect);
  }

  @NonNull
  private static Optional<ZonedDateTime> findPreviewDate() {
    return CurrentStoreContext.find()
            .flatMap(StoreContext::getPreviewDate);
  }
}
