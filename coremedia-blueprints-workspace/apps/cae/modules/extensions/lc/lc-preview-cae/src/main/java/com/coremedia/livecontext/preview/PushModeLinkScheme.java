package com.coremedia.livecontext.preview;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CommerceConnectionSupplier;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.link.PreviewUrlService;
import com.coremedia.livecontext.ecommerce.link.QueryParam;
import com.coremedia.livecontext.fragment.links.transformers.resolvers.seo.ExternalSeoSegmentBuilder;
import com.coremedia.objectserver.web.links.Link;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.web.util.UriComponents;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.blueprint.base.livecontext.ecommerce.link.UrlUtil.convertToQueryParamList;

@Named
@Link
@DefaultAnnotation(NonNull.class)
public class PushModeLinkScheme {

  private final ExternalSeoSegmentBuilder externalSeoSegmentBuilder;
  private final CommerceConnectionSupplier commerceConnectionSupplier;
  private final ContextHelper contextHelper;

  public PushModeLinkScheme(ExternalSeoSegmentBuilder externalSeoSegmentBuilder, CommerceConnectionSupplier commerceConnectionSupplier, ContextHelper contextHelper) {
    this.externalSeoSegmentBuilder = externalSeoSegmentBuilder;
    this.commerceConnectionSupplier = commerceConnectionSupplier;
    this.contextHelper = contextHelper;
  }

  @Link(type = CMLinkable.class, order = 2, view = "pushMode")
  @Nullable
  public UriComponents buildLinkForLinkablesInPushMode(CMLinkable linkable, @Nullable String viewName, @Nullable Map<String, Object> linkParameters,
                                                       HttpServletRequest request) {

    Optional<CommerceConnection> connection = commerceConnectionSupplier.findConnection(linkable.getContent());

    if (!connection.isPresent()
            || !connection.get().getPushService().isPresent()) {
      return null;
    }

    Optional<PreviewUrlService> previewUrlService = connection.get().getPreviewUrlService();
    if (!previewUrlService.isPresent()) {
      return null;
    }

    String seoPath = externalSeoSegmentBuilder.asSeoSegment(contextHelper.contextFor(linkable), linkable);
    List<QueryParam> queryParamList = convertToQueryParamList(linkParameters);
    return previewUrlService.get().getContentUrl(seoPath, connection.get().getStoreContext(), queryParamList, request);
  }
}
