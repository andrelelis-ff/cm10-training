package com.coremedia.livecontext.preview;

import com.coremedia.blueprint.base.links.UriConstants;
import com.coremedia.blueprint.cae.handlers.HandlerBase;
import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.blueprint.common.contentbeans.CMNavigation;
import com.coremedia.blueprint.common.services.context.ContextHelper;
import com.coremedia.livecontext.fragment.links.transformers.resolvers.seo.ExternalSeoSegmentBuilder;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@RequestMapping
@DefaultAnnotation(NonNull.class)
public class SeoSegmentBuilderHandler extends HandlerBase {

  private static final String URI_PREFIX = "seoSegment";
  private static final String ID_VARIABLE = "id";
  private static final String URI_PATTERN = "/" + URI_PREFIX + "/{" + ID_VARIABLE + ":" + UriConstants.Patterns.PATTERN_NUMBER + "}";

  private final ContextHelper contextHelper;
  private final ExternalSeoSegmentBuilder externalSeoSegmentBuilder;

  public SeoSegmentBuilderHandler(ContextHelper contextHelper, ExternalSeoSegmentBuilder externalSeoSegmentBuilder) {
    this.contextHelper = contextHelper;
    this.externalSeoSegmentBuilder = externalSeoSegmentBuilder;
  }

  @GetMapping(URI_PATTERN)
  @Produces(MediaType.APPLICATION_JSON)
  @ResponseBody
  public String getSeoSegment(@PathVariable(ID_VARIABLE) CMLinkable linkable,
                              HttpServletRequest request){
    if (linkable == null){
      throw new IllegalArgumentException("Invalid content id. Could not find linkable.");
    }

    CMNavigation cmNavigation = contextHelper.contextFor(linkable);

    return externalSeoSegmentBuilder.asSeoSegment(cmNavigation, linkable);
  }
}
