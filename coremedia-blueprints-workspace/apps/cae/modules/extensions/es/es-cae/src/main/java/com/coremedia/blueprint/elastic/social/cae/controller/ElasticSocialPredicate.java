package com.coremedia.blueprint.elastic.social.cae.controller;

import com.coremedia.blueprint.cae.view.DynamicIncludePredicate;
import com.coremedia.objectserver.view.RenderNode;

import edu.umd.cs.findbugs.annotations.Nullable;
import javax.inject.Named;

@Named
public class ElasticSocialPredicate implements DynamicIncludePredicate {
  @Override
  public boolean apply(@Nullable RenderNode input) {
    return input != null &&
            (input.getBean() instanceof CommentsResult
                    || input.getBean() instanceof ReviewsResult
                    || input.getBean() instanceof ComplaintResult
                    || input.getBean() instanceof RatingResult
                    || input.getBean() instanceof ShareResult
                    || input.getBean() instanceof LikeResult);
  }
}
